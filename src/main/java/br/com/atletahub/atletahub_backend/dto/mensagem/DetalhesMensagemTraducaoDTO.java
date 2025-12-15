package br.com.atletahub.atletahub_backend.dto.mensagem;

import br.com.atletahub.atletahub_backend.model.MensagemTraducao;

public record DetalhesMensagemTraducaoDTO(
        Long id,
        Long idMensagem,
        String idiomaOrigem,
        String idiomaDestino,
        String textoTraduzido
) {
    public DetalhesMensagemTraducaoDTO(MensagemTraducao traducao) {
        this(
                traducao.getId(),
                traducao.getMensagem().getId(),
                traducao.getIdiomaOrigem(),
                traducao.getIdiomaDestino(),
                traducao.getTextoTraduzido()
        );
    }
}

