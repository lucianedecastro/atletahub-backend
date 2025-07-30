package br.com.atletahub.atletahub_backend.dto.perfil;

import br.com.atletahub.atletahub_backend.model.PerfilMarca;


public record DadosDetalhamentoPerfilMarca(

        Long idPerfilMarca,
        Long idUsuario,
        String produto,
        Integer tempoMercado,
        String atletasPatrocinados,
        String tipoInvestimento,
        String redesSocial

) {
    public DadosDetalhamentoPerfilMarca(PerfilMarca perfil) {
        this(
                perfil.getId_perfil_marca(),
                perfil.getUsuario() != null ? perfil.getUsuario().getIdUsuario() : null,
                perfil.getProduto(),
                perfil.getTempoMercado(),
                perfil.getAtletasPatrocinados(),
                perfil.getTipoInvestimento(),
                perfil.getRedesSocial()
        );
    }
}