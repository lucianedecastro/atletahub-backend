package br.com.atletahub.atletahub_backend.service;

import br.com.atletahub.atletahub_backend.dto.denuncia.DadosCriacaoDenuncia;
import br.com.atletahub.atletahub_backend.enums.StatusDenuncia;
import br.com.atletahub.atletahub_backend.enums.TipoAlvoDenuncia;
import br.com.atletahub.atletahub_backend.model.Denuncia;
import br.com.atletahub.atletahub_backend.model.Mensagem;
import br.com.atletahub.atletahub_backend.model.TipoUsuario;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.repository.DenunciaRepository;
import br.com.atletahub.atletahub_backend.repository.MensagemRepository;
import br.com.atletahub.atletahub_backend.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class DenunciaService {

    private static final Logger logger = LoggerFactory.getLogger(DenunciaService.class);

    // Freio contra denúncia em massa.
    private static final int MAX_POR_DIA = 10;

    @Autowired private DenunciaRepository denunciaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private MensagemRepository mensagemRepository;

    @Transactional
    public void criar(Usuario denunciante, DadosCriacaoDenuncia dados) {
        if (denunciante.getIdUsuario().equals(dados.idDenunciado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Você não pode denunciar a si mesmo.");
        }

        Usuario denunciado = usuarioRepository.findById(dados.idDenunciado())
                .filter(u -> u.getTipoUsuario() != TipoUsuario.ADMIN)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        long hoje = denunciaRepository.countByIdDenuncianteAndCriadaEmAfter(
                denunciante.getIdUsuario(), Instant.now().minus(1, ChronoUnit.DAYS));
        if (hoje >= MAX_POR_DIA) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Você já enviou muitas denúncias hoje. Tente novamente amanhã.");
        }

        if (denunciaRepository.existsByIdDenuncianteAndIdDenunciadoAndTipoAlvoAndStatus(
                denunciante.getIdUsuario(), denunciado.getIdUsuario(), dados.tipoAlvo(), StatusDenuncia.ABERTA)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Você já enviou uma denúncia sobre isso. Ela está em análise.");
        }

        String referencia = dados.referencia() == null || dados.referencia().isBlank() ? null : dados.referencia().trim();
        String trecho = null;

        if (dados.tipoAlvo() == TipoAlvoDenuncia.MENSAGEM) {
            Mensagem mensagem = buscarMensagemDaConversa(denunciante, denunciado, referencia);
            // Guarda uma cópia do texto: quem escreveu pode apagar depois, e o admin precisa ver o que foi denunciado.
            trecho = limitar(mensagem.getTexto(), 2000);
        } else if (dados.tipoAlvo() == TipoAlvoDenuncia.MIDIA) {
            if (referencia == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe qual foto ou vídeo está sendo denunciado.");
            }
            trecho = referencia;
        }

        Denuncia d = new Denuncia();
        d.setIdDenunciante(denunciante.getIdUsuario());
        d.setIdDenunciado(denunciado.getIdUsuario());
        d.setTipoAlvo(dados.tipoAlvo());
        d.setMotivo(dados.motivo());
        d.setDescricao(dados.descricao() == null || dados.descricao().isBlank() ? null : dados.descricao().trim());
        d.setReferencia(referencia);
        d.setTrecho(trecho);
        denunciaRepository.save(d);

        // Sem dados pessoais no log.
        logger.info("Denúncia registrada (tipo={}, motivo={})", dados.tipoAlvo(), dados.motivo());
    }

    private Mensagem buscarMensagemDaConversa(Usuario denunciante, Usuario denunciado, String referencia) {
        if (referencia == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe qual mensagem está sendo denunciada.");
        }
        long idMensagem;
        try {
            idMensagem = Long.parseLong(referencia);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mensagem inválida.");
        }

        Mensagem mensagem = mensagemRepository.findById(idMensagem)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensagem não encontrada."));

        Long a = mensagem.getMatch().getIdUsuarioA();
        Long b = mensagem.getMatch().getIdUsuarioB();
        boolean participa = denunciante.getIdUsuario().equals(a) || denunciante.getIdUsuario().equals(b);
        boolean autorEhDenunciado = mensagem.getRemetente().getIdUsuario().equals(denunciado.getIdUsuario());
        // Só quem está na conversa pode denunciar, e só mensagens escritas pela pessoa denunciada.
        if (!participa || !autorEhDenunciado) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensagem não encontrada.");
        }
        return mensagem;
    }

    private static String limitar(String texto, int max) {
        return texto == null ? null : (texto.length() <= max ? texto : texto.substring(0, max));
    }
}
