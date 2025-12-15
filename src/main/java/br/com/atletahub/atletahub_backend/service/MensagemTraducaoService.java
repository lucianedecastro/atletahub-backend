package br.com.atletahub.atletahub_backend.service;

import br.com.atletahub.atletahub_backend.dto.mensagem.DadosCriacaoMensagemTraducaoDTO;
import br.com.atletahub.atletahub_backend.dto.mensagem.DetalhesMensagemTraducaoDTO;
import br.com.atletahub.atletahub_backend.model.Mensagem;
import br.com.atletahub.atletahub_backend.model.MensagemTraducao;
import br.com.atletahub.atletahub_backend.repository.MensagemRepository;
import br.com.atletahub.atletahub_backend.repository.MensagemTraducaoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MensagemTraducaoService {

    @Autowired
    private MensagemRepository mensagemRepository;

    @Autowired
    private MensagemTraducaoRepository mensagemTraducaoRepository;

    @Transactional
    public DetalhesMensagemTraducaoDTO traduzirMensagem(
            DadosCriacaoMensagemTraducaoDTO dados
    ) {

        // 1️⃣ Busca a mensagem original
        Mensagem mensagem = mensagemRepository.findById(dados.idMensagem())
                .orElseThrow(() ->
                        new IllegalArgumentException("Mensagem não encontrada.")
                );

        // 2️⃣ Verifica se a tradução já existe
        MensagemTraducao traducaoExistente =
                mensagemTraducaoRepository
                        .findByMensagem_IdAndIdiomaDestino(
                                dados.idMensagem(),
                                dados.idiomaDestino()
                        );

        if (traducaoExistente != null) {
            return new DetalhesMensagemTraducaoDTO(traducaoExistente);
        }

        // 3️⃣ (mock por enquanto) tradução simulada
        String textoTraduzido = "[TRADUZIDO] " + mensagem.getTexto();

        // 4️⃣ Cria nova tradução — CONSTRUTOR CORRETO
        MensagemTraducao novaTraducao = new MensagemTraducao(
                mensagem,
                dados.idiomaOrigem(),
                dados.idiomaDestino(),
                textoTraduzido
        );

        mensagemTraducaoRepository.save(novaTraducao);

        return new DetalhesMensagemTraducaoDTO(novaTraducao);
    }
}
