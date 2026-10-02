package br.com.atletahub.atletahub_backend.service;

import br.com.atletahub.atletahub_backend.dto.interesse.DadosCadastroInteresse;
import br.com.atletahub.atletahub_backend.enums.TipoInteresse;
import br.com.atletahub.atletahub_backend.model.Interesse;
import br.com.atletahub.atletahub_backend.model.Match;
import br.com.atletahub.atletahub_backend.model.TipoUsuario;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.repository.InteresseRepository;
import br.com.atletahub.atletahub_backend.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InteresseService {

    private static final Logger logger = LoggerFactory.getLogger(InteresseService.class);

    @Autowired
    private InteresseRepository interesseRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MatchService matchService;

    @Autowired
    private BloqueioService bloqueioService;

    @Transactional
    public Interesse registrarInteresse(Long idOrigem, DadosCadastroInteresse dados) {
        Usuario origem = usuarioRepository.findById(idOrigem)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário de origem não encontrado."));

        Usuario destino = usuarioRepository.findById(dados.idDestino())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário de destino não encontrado."));

        if (origem.getIdUsuario().equals(destino.getIdUsuario())) {
            throw new IllegalArgumentException("Não é permitido demonstrar interesse em si mesmo.");
        }

        // Bloqueio em qualquer direção: responde como se a pessoa não existisse (não revela o bloqueio).
        if (bloqueioService.existeEntre(origem.getIdUsuario(), destino.getIdUsuario())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário de destino não encontrado.");
        }

        // A plataforma conecta ATLETAS com MARCAS: atleta↔atleta e marca↔marca não fazem sentido.
        validarParAtletaMarca(origem, destino);

        Optional<Interesse> existente = interesseRepository
                .findByOrigem_IdUsuarioAndDestino_IdUsuario(origem.getIdUsuario(), destino.getIdUsuario());

        Interesse interesseSalvo;

        if (existente.isPresent()) {
            Interesse atual = existente.get();

            // Único caminho permitido sobre um interesse existente: subir de CURTIR para SUPER_CURTIR.
            boolean upgrade = atual.getTipoInteresse() == TipoInteresse.CURTIR
                    && dados.tipoInteresse() == TipoInteresse.SUPER_CURTIR;

            if (!upgrade) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Você já demonstrou interesse neste usuário.");
            }

            atual.atualizar(TipoInteresse.SUPER_CURTIR);
            interesseSalvo = interesseRepository.save(atual);
        } else {
            interesseSalvo = interesseRepository.save(new Interesse(origem, destino, dados.tipoInteresse()));
        }

        Match matchGerado = matchService.verificarEGerarMatch(origem, destino, interesseSalvo.getTipoInteresse());
        if (matchGerado != null) {
            logger.info("Match gerado (id={}, tipo={})", matchGerado.getId(), matchGerado.getTipoMatch());
        }

        return interesseSalvo;
    }

    private void validarParAtletaMarca(Usuario origem, Usuario destino) {
        boolean origemAtleta = origem.getTipoUsuario() == TipoUsuario.ATLETA;
        boolean origemMarca = origem.getTipoUsuario() == TipoUsuario.MARCA;
        boolean destinoAtleta = destino.getTipoUsuario() == TipoUsuario.ATLETA;
        boolean destinoMarca = destino.getTipoUsuario() == TipoUsuario.MARCA;

        boolean par = (origemAtleta && destinoMarca) || (origemMarca && destinoAtleta);
        if (!par) {
            throw new IllegalArgumentException("Atletas só podem demonstrar interesse em marcas, e marcas em atletas.");
        }
    }

    public Interesse buscarInteressePorId(Long idInteresse) {
        return interesseRepository.findById(idInteresse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interesse não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<Interesse> listarInteressesEnviados(Long idOrigem) {
        Set<Long> ocultos = bloqueioService.idsComBloqueio(idOrigem);
        return interesseRepository.findByOrigem_IdUsuario(idOrigem).stream()
                .filter(i -> !ocultos.contains(i.getDestino().getIdUsuario()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Interesse> listarInteressesRecebidos(Long idDestino) {
        Set<Long> ocultos = bloqueioService.idsComBloqueio(idDestino);
        return interesseRepository.findByDestino_IdUsuario(idDestino).stream()
                .filter(i -> !ocultos.contains(i.getOrigem().getIdUsuario()))
                .collect(Collectors.toList());
    }
}
