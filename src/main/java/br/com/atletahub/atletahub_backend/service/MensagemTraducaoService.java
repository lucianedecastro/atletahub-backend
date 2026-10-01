package br.com.atletahub.atletahub_backend.service;

import br.com.atletahub.atletahub_backend.dto.mensagem.DadosCriacaoMensagemTraducaoDTO;
import br.com.atletahub.atletahub_backend.dto.mensagem.DetalhesMensagemTraducaoDTO;
import br.com.atletahub.atletahub_backend.model.Match;
import br.com.atletahub.atletahub_backend.model.Mensagem;
import br.com.atletahub.atletahub_backend.model.MensagemTraducao;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.repository.MensagemRepository;
import br.com.atletahub.atletahub_backend.repository.MensagemTraducaoRepository;
import br.com.atletahub.atletahub_backend.repository.UsuarioRepository;
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
    private final UsuarioRepository usuarioRepository;

    // A AWS Translate detecta o idioma de origem quando recebe "auto".
    private static final String ORIGEM_AUTOMATICA = "auto";

    public MensagemTraducaoService(MensagemRepository mensagemRepository,
                                   MensagemTraducaoRepository mensagemTraducaoRepository,
                                   TraducaoProvider traducaoProvider,
                                   TraducaoMetricService traducaoMetricService,
                                   TraducaoProperties traducaoProperties,
                                   UsuarioRepository usuarioRepository) {
        this.mensagemRepository = mensagemRepository;
        this.mensagemTraducaoRepository = mensagemTraducaoRepository;
        this.traducaoProvider = traducaoProvider;
        this.traducaoMetricService = traducaoMetricService;
        this.traducaoProperties = traducaoProperties;
        this.usuarioRepository = usuarioRepository;
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

        // Origem: sempre detectada pelo servidor (o front antigo só distinguia português de inglês).
        // Destino: o idioma de preferência de quem pediu; só cai no que veio na requisição ou no
        // padrão do sistema se a conta não tiver idioma.
        String destino = normalizar(buscarIdiomaPreferido(idUsuario));
        if (destino == null) destino = normalizar(dados.idiomaDestino());
        if (destino == null) destino = traducaoProperties.getIdiomaPadraoDestino();

        return executar(mensagem, ORIGEM_AUTOMATICA, destino);
    }

    /**
     * Uso INTERNO (tradução automática pós-envio, disparada pelo sistema, sem usuário).
     * NÃO expor em controller.
     */
    @Transactional
    public DetalhesMensagemTraducaoDTO traduzirMensagemInterno(DadosCriacaoMensagemTraducaoDTO dados) {
        String origem = normalizar(dados.idiomaOrigem());
        String destino = normalizar(dados.idiomaDestino());
        return executar(
                buscarMensagem(dados.idMensagem()),
                origem != null ? origem : ORIGEM_AUTOMATICA,
                destino != null ? destino : traducaoProperties.getIdiomaPadraoDestino());
    }

    private String buscarIdiomaPreferido(Long idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .map(Usuario::getIdiomaPreferencia)
                .orElse(null);
    }

    // "PT-br " -> "pt-br"; vazio vira null.
    private static String normalizar(String idioma) {
        if (idioma == null || idioma.isBlank()) return null;
        return idioma.trim().toLowerCase();
    }

    private Mensagem buscarMensagem(Long idMensagem) {
        return mensagemRepository.findById(idMensagem)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensagem não encontrada."));
    }

    private DetalhesMensagemTraducaoDTO executar(Mensagem mensagem, String idiomaOrigem, String idiomaDestino) {

        // 🔒 FEATURE FLAG
        if (!traducaoProperties.isAutomatica()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Tradução está desativada no sistema.");
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
                    idiomaOrigem,
                    idiomaDestino
            );
        } catch (RuntimeException e) {
            String detalhe = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (ORIGEM_AUTOMATICA.equals(idiomaOrigem) && detalhe.contains("same")) {
                // A mensagem já está no idioma de quem lê: não há o que traduzir.
                // Guarda o texto original como "tradução" para não chamar a AWS de novo.
                logger.info("Mensagem {} já está em {}: sem tradução", mensagem.getId(), idiomaDestino);
                textoTraduzido = mensagem.getTexto();
            } else {
                // 502 (e não 500): o problema é o serviço externo. O app mostra "tente de novo" sem deslogar.
                logger.warn("Falha no provedor de tradução (mensagem {}, {} -> {}): {}",
                        mensagem.getId(), idiomaOrigem, idiomaDestino, e.getMessage());
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Serviço de tradução indisponível no momento.");
            }
        }

        // Métricas
        traducaoMetricService.registrarTraducao(
                idiomaOrigem,
                idiomaDestino,
                mensagem.getTexto().length(),
                inicio
        );

        // Persistência
        MensagemTraducao novaTraducao = new MensagemTraducao(
                mensagem,
                idiomaOrigem,
                idiomaDestino,
                textoTraduzido
        );

        mensagemTraducaoRepository.save(novaTraducao);

        return new DetalhesMensagemTraducaoDTO(novaTraducao);
    }
}
