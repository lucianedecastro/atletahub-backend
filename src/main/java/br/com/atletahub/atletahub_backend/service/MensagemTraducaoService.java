package br.com.atletahub.atletahub_backend.service;

import br.com.atletahub.atletahub_backend.dto.mensagem.DadosCriacaoMensagemTraducaoDTO;
import br.com.atletahub.atletahub_backend.dto.mensagem.DetalhesMensagemTraducaoDTO;
import br.com.atletahub.atletahub_backend.model.Mensagem;
import br.com.atletahub.atletahub_backend.model.MensagemTraducao;
import br.com.atletahub.atletahub_backend.repository.MensagemRepository;
import br.com.atletahub.atletahub_backend.repository.MensagemTraducaoRepository;
import br.com.atletahub.atletahub_backend.traducao.config.TraducaoProperties;
import br.com.atletahub.atletahub_backend.traducao.metric.TraducaoMetricService;
import br.com.atletahub.atletahub_backend.traducao.provider.TraducaoProvider;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class MensagemTraducaoService {

    @Autowired
    private MensagemRepository mensagemRepository;

    @Autowired
    private MensagemTraducaoRepository mensagemTraducaoRepository;

    @Autowired
    private TraducaoProvider traducaoProvider;

    @Autowired
    private TraducaoMetricService traducaoMetricService;

    @Autowired
    private TraducaoProperties traducaoProperties;

    @Transactional
    public DetalhesMensagemTraducaoDTO traduzirMensagem(
            DadosCriacaoMensagemTraducaoDTO dados
    ) {

        // 🔒 FEATURE FLAG
        if (!traducaoProperties.isAutomatica()) {
            throw new IllegalStateException(
                    "Tradução automática está desativada no sistema."
            );
        }

        // 🌍 Resolve idioma destino
        String idiomaDestino = dados.idiomaDestino();
        if (idiomaDestino == null || idiomaDestino.isBlank()) {
            idiomaDestino = traducaoProperties.getIdiomaPadraoDestino();
        }

        // 1️⃣ Busca mensagem original
        Mensagem mensagem = mensagemRepository.findById(dados.idMensagem())
                .orElseThrow(() ->
                        new IllegalArgumentException("Mensagem não encontrada.")
                );

        // 2️⃣ Cache por par de idiomas (origem + destino)
        MensagemTraducao traducaoExistente =
                mensagemTraducaoRepository
                        .findByMensagem_IdAndIdiomaDestino(
                                dados.idMensagem(),
                                idiomaDestino
                        );

        if (traducaoExistente != null) {
            return new DetalhesMensagemTraducaoDTO(traducaoExistente);
        }

        // 3️⃣ Tradução REAL
        Instant inicio = Instant.now();

        String textoTraduzido = traducaoProvider.traduzir(
                mensagem.getTexto(),
                dados.idiomaOrigem(),
                idiomaDestino
        );

        // 4️⃣ Métricas
        traducaoMetricService.registrarTraducao(
                dados.idiomaOrigem(),
                idiomaDestino,
                mensagem.getTexto().length(),
                inicio
        );

        // 5️⃣ Persistência
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
