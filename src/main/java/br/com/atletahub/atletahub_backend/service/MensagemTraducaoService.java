package br.com.atletahub.atletahub_backend.service;

import br.com.atletahub.atletahub_backend.dto.mensagem.DadosCriacaoMensagemTraducaoDTO;
import br.com.atletahub.atletahub_backend.dto.mensagem.DetalhesMensagemTraducaoDTO;
import br.com.atletahub.atletahub_backend.model.Match;
import br.com.atletahub.atletahub_backend.model.Mensagem;
import br.com.atletahub.atletahub_backend.model.MensagemTraducao;
import br.com.atletahub.atletahub_backend.repository.MensagemRepository;
import br.com.atletahub.atletahub_backend.repository.MensagemTraducaoRepository;
import br.com.atletahub.atletahub_backend.traducao.config.TraducaoProperties;
import br.com.atletahub.atletahub_backend.traducao.metric.TraducaoMetricService;
import br.com.atletahub.atletahub_backend.traducao.provider.TraducaoProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
public class MensagemTraducaoService {

    private static final Logger logger = LoggerFactory.getLogger(MensagemTraducaoService.class);

    private final MensagemRepository mensagemRepository;
    private final MensagemTraducaoRepository mensagemTraducaoRepository;
    private final TraducaoProvider traducaoProvider;
    private final TraducaoMetricService traducaoMetricService;
    private final TraducaoProperties traducaoProperties;

    public MensagemTraducaoService(MensagemRepository mensagemRepository,
                                   MensagemTraducaoRepository mensagemTraducaoRepository,
                                   TraducaoProvider traducaoProvider,
                                   TraducaoMetricService traducaoMetricService,
                                   TraducaoProperties traducaoProperties) {
        this.mensagemRepository = mensagemRepository;
        this.mensagemTraducaoRepository = mensagemTraducaoRepository;
        this.traducaoProvider = traducaoProvider;
        this.traducaoMetricService = traducaoMetricService;
        this.traducaoProperties = traducaoProperties;
    }

    /**
     * Tradução SOB DEMANDA, pedida por um usuário.
     * Só quem participa do match da mensagem pode traduzi-la.
     */
    @Transactional
    public DetalhesMensagemTraducaoDTO traduzirMensagem(Long idUsuario, DadosCriacaoMensagemTraducaoDTO dados) {
        Mensagem mensagem = buscarMensagem(dados.idMensagem());

        Match match = mensagem.getMatch();
        boolean participa = idUsuario.equals(match.getIdUsuarioA()) || idUsuario.equals(match.getIdUsuarioB());
        if (!participa) {
            // 404 (e não 403): não revela que a mensagem existe e não desloga o usuário no front.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensagem não encontrada.");
        }

        return executar(mensagem, dados);
    }

    /**
     * Uso INTERNO (tradução automática pós-envio, disparada pelo sistema, sem usuário).
     * NÃO expor em controller.
     */
    @Transactional
    public DetalhesMensagemTraducaoDTO traduzirMensagemInterno(DadosCriacaoMensagemTraducaoDTO dados) {
        return executar(buscarMensagem(dados.idMensagem()), dados);
    }

    private Mensagem buscarMensagem(Long idMensagem) {
        return mensagemRepository.findById(idMensagem)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensagem não encontrada."));
    }

    private DetalhesMensagemTraducaoDTO executar(Mensagem mensagem, DadosCriacaoMensagemTraducaoDTO dados) {

        // 🔒 FEATURE FLAG
        if (!traducaoProperties.isAutomatica()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Tradução está desativada no sistema.");
        }

        // 🌍 Resolve idioma destino
        String idiomaDestino = dados.idiomaDestino();
        if (idiomaDestino == null || idiomaDestino.isBlank()) {
            idiomaDestino = traducaoProperties.getIdiomaPadraoDestino();
        }

        // Cache por mensagem + idioma de destino
        MensagemTraducao traducaoExistente =
                mensagemTraducaoRepository.findByMensagem_IdAndIdiomaDestino(mensagem.getId(), idiomaDestino);

        if (traducaoExistente != null) {
            return new DetalhesMensagemTraducaoDTO(traducaoExistente);
        }

        // Tradução REAL
        Instant inicio = Instant.now();
        String textoTraduzido;
        try {
            textoTraduzido = traducaoProvider.traduzir(
                    mensagem.getTexto(),
                    dados.idiomaOrigem(),
                    idiomaDestino
            );
        } catch (RuntimeException e) {
            // 502 (e não 500): o problema é o serviço externo. O app mostra "tente de novo" sem deslogar.
            logger.warn("Falha no provedor de tradução (mensagem {}): {}", mensagem.getId(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Serviço de tradução indisponível no momento.");
        }

        // Métricas
        traducaoMetricService.registrarTraducao(
                dados.idiomaOrigem(),
                idiomaDestino,
                mensagem.getTexto().length(),
                inicio
        );

        // Persistência
        MensagemTraducao novaTraducao = new MensagemTraducao(
                mensagem,
                dados.idiomaOrigem(),
                idiomaDestino,
                textoTraduzido
        );

        mensagemTraducaoRepository.save(novaTraducao);

        return new DetalhesMensagemTraducaoDTO(novaTraducao);
    }
}
