package br.com.atletahub.atletahub_backend.service;

import br.com.atletahub.atletahub_backend.dto.mensagem.DadosEnvioMensagemDTO;
import br.com.atletahub.atletahub_backend.dto.mensagem.DetalhesMensagemDTO;
import br.com.atletahub.atletahub_backend.model.Match;
import br.com.atletahub.atletahub_backend.model.Mensagem;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.repository.MatchRepository;
import br.com.atletahub.atletahub_backend.repository.MensagemRepository;
import br.com.atletahub.atletahub_backend.repository.UsuarioRepository;
import br.com.atletahub.atletahub_backend.traducao.config.TraducaoProperties;
import br.com.atletahub.atletahub_backend.traducao.event.MensagemEnviadaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MensagemService {

    private static final Logger logger = LoggerFactory.getLogger(MensagemService.class);

    private final MensagemRepository mensagemRepository;
    private final MatchRepository matchRepository;
    private final UsuarioRepository usuarioRepository;
    private final TraducaoProperties traducaoProperties;
    private final ApplicationEventPublisher eventPublisher;
    private final BloqueioService bloqueioService;

    public MensagemService(MensagemRepository mensagemRepository,
                           MatchRepository matchRepository,
                           UsuarioRepository usuarioRepository,
                           TraducaoProperties traducaoProperties,
                           ApplicationEventPublisher eventPublisher,
                           BloqueioService bloqueioService) {
        this.mensagemRepository = mensagemRepository;
        this.matchRepository = matchRepository;
        this.usuarioRepository = usuarioRepository;
        this.traducaoProperties = traducaoProperties;
        this.eventPublisher = eventPublisher;
        this.bloqueioService = bloqueioService;
    }

    /**
     * @param idRemetente ID do usuário autenticado (vem do token, nunca do corpo da requisição).
     */
    @Transactional
    public DetalhesMensagemDTO enviarMensagem(Long idRemetente, DadosEnvioMensagemDTO dados) {

        // 1. O match precisa existir E o usuário precisa participar dele.
        Match match = buscarMatchDoParticipante(dados.idMatch(), idRemetente);

        Usuario remetente = usuarioRepository.findById(idRemetente)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado."));

        // 2. Descobre o destinatário
        Usuario destinatario = idRemetente.equals(match.getIdUsuarioA())
                ? match.getUsuarioB()
                : match.getUsuarioA();

        // Conversa congelada: com bloqueio (em qualquer direção) não entra mensagem nova.
        // A resposta é genérica (422, que não desloga a pessoa) e não diz quem bloqueou.
        if (bloqueioService.existeEntre(remetente.getIdUsuario(), destinatario.getIdUsuario())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Não foi possível enviar esta mensagem.");
        }

        // 3. Salva a mensagem original
        Mensagem mensagemSalva = mensagemRepository.save(new Mensagem(match, remetente, dados.texto()));

        // 4. Tradução automática: NÃO roda aqui dentro.
        //    Só avisamos que a mensagem foi enviada; a tradução acontece depois do commit,
        //    em outra thread (ver TraducaoAutomaticaListener). Assim uma falha da AWS
        //    nunca derruba nem trava o envio da mensagem.
        publicarTraducaoAutomatica(mensagemSalva, remetente, destinatario);

        return new DetalhesMensagemDTO(mensagemSalva);
    }

    private void publicarTraducaoAutomatica(Mensagem mensagem, Usuario remetente, Usuario destinatario) {
        if (!traducaoProperties.isAutomatica()) {
            return;
        }

        String langOrigem = remetente.getIdiomaPreferencia() != null ? remetente.getIdiomaPreferencia() : "pt";
        String langDestino = destinatario.getIdiomaPreferencia() != null ? destinatario.getIdiomaPreferencia() : "pt";

        // Só traduz se os idiomas forem diferentes
        if (!langOrigem.equalsIgnoreCase(langDestino)) {
            logger.info("Tradução automática agendada: mensagem {} de {} para {}",
                    mensagem.getId(), langOrigem, langDestino);
            eventPublisher.publishEvent(new MensagemEnviadaEvent(mensagem.getId(), langOrigem, langDestino));
        }
    }

    /**
     * @param idUsuario ID do usuário autenticado: só quem participa do match pode ler a conversa.
     */
    @Transactional(readOnly = true)
    public List<DetalhesMensagemDTO> listarMensagensDoMatch(Long idMatch, Long idUsuario) {

        buscarMatchDoParticipante(idMatch, idUsuario);

        return mensagemRepository
                .findByMatch_IdOrderByDataEnvioAsc(idMatch)
                .stream()
                .map(DetalhesMensagemDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Devolve o match se ele existe e o usuário participa dele.
     * Nos dois casos de falha a resposta é 404 (e não 403): não revela se o match existe
     * e evita que o front, que desloga o usuário em qualquer 403, derrube a sessão.
     */
    private Match buscarMatchDoParticipante(Long idMatch, Long idUsuario) {
        Match match = matchRepository.findById(idMatch)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match não encontrado."));

        boolean participa = idUsuario.equals(match.getIdUsuarioA()) || idUsuario.equals(match.getIdUsuarioB());
        if (!participa) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Match não encontrado.");
        }
        return match;
    }
}
