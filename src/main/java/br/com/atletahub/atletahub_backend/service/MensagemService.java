package br.com.atletahub.atletahub_backend.service;

import br.com.atletahub.atletahub_backend.dto.mensagem.DadosCriacaoMensagemTraducaoDTO;
import br.com.atletahub.atletahub_backend.dto.mensagem.DadosEnvioMensagemDTO;
import br.com.atletahub.atletahub_backend.dto.mensagem.DetalhesMensagemDTO;
import br.com.atletahub.atletahub_backend.model.Match;
import br.com.atletahub.atletahub_backend.model.Mensagem;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.repository.MatchRepository;
import br.com.atletahub.atletahub_backend.repository.MensagemRepository;
import br.com.atletahub.atletahub_backend.repository.UsuarioRepository;
import br.com.atletahub.atletahub_backend.traducao.config.TraducaoProperties;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MensagemService {

    private static final Logger logger = LoggerFactory.getLogger(MensagemService.class);

    @Autowired
    private MensagemRepository mensagemRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MensagemTraducaoService mensagemTraducaoService;

    @Autowired
    private TraducaoProperties traducaoProperties;

    @Transactional
    public DetalhesMensagemDTO enviarMensagem(DadosEnvioMensagemDTO dados) {

        // 1. Validações Básicas
        Match match = matchRepository.findById(dados.idMatch())
                .orElseThrow(() ->
                        new IllegalArgumentException("Match não encontrado. ID: " + dados.idMatch())
                );

        Usuario remetente = usuarioRepository.findById(dados.idRemetente())
                .orElseThrow(() ->
                        new IllegalArgumentException("Usuário remetente não encontrado. ID: " + dados.idRemetente())
                );

        // 2. Verifica participação no Match e descobre o Destinatário
        Usuario destinatario;
        if (match.getUsuarioA().getIdUsuario().equals(remetente.getIdUsuario())) {
            destinatario = match.getUsuarioB();
        } else if (match.getUsuarioB().getIdUsuario().equals(remetente.getIdUsuario())) {
            destinatario = match.getUsuarioA();
        } else {
            throw new IllegalArgumentException("O usuário não participa deste match.");
        }

        // 3. Salva a Mensagem Original
        Mensagem novaMensagem = new Mensagem(match, remetente, dados.texto());
        Mensagem mensagemSalva = mensagemRepository.save(novaMensagem);

        // 4. 🌍 Fluxo de Tradução Automática
        try {
            processarTraducaoAutomatica(mensagemSalva, remetente, destinatario);
        } catch (Exception e) {
            // Loga o erro mas NÃO trava o envio da mensagem original
            logger.error("Erro ao processar tradução automática: {}", e.getMessage());
        }

        return new DetalhesMensagemDTO(mensagemSalva);
    }

    private void processarTraducaoAutomatica(Mensagem mensagem, Usuario remetente, Usuario destinatario) {
        // Verifica se a tradução automática está ligada no properties
        if (!traducaoProperties.isAutomatica()) {
            return;
        }

        String langOrigem = remetente.getIdiomaPreferencia();
        String langDestino = destinatario.getIdiomaPreferencia();

        // Garante que não sejam nulos (fallback para 'pt' se necessário, ou ignora)
        if (langOrigem == null) langOrigem = "pt";
        if (langDestino == null) langDestino = "pt";

        // Só traduz se os idiomas forem diferentes
        if (!langOrigem.equalsIgnoreCase(langDestino)) {
            logger.info("Traduzindo mensagem ID {} de {} para {}", mensagem.getId(), langOrigem, langDestino);

            DadosCriacaoMensagemTraducaoDTO dadosTraducao = new DadosCriacaoMensagemTraducaoDTO(
                    mensagem.getId(),
                    langOrigem,
                    langDestino
            );

            mensagemTraducaoService.traduzirMensagem(dadosTraducao);
        }
    }

    @Transactional
    public List<DetalhesMensagemDTO> listarMensagensDoMatch(Long idMatch) {

        if (!matchRepository.existsById(idMatch)) {
            throw new IllegalArgumentException("Match não encontrado. ID: " + idMatch);
        }

        return mensagemRepository
                .findByMatch_IdOrderByDataEnvioAsc(idMatch)
                .stream()
                .map(DetalhesMensagemDTO::new)
                .collect(Collectors.toList());
    }
}