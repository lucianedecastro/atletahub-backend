package br.com.atletahub.atletahub_backend.dto.perfil;

import br.com.atletahub.atletahub_backend.model.PerfilMarca;

public record DadosDetalhamentoPerfilMarca(
        Long idPerfilMarca,
        Long idUsuario, // Apenas o ID
        String produto,
        Integer tempoMercado,
        String atletasPatrocinados,
        String tipoInvestimento,
        String redesSocial
) {
    public DadosDetalhamentoPerfilMarca(PerfilMarca perfil) {
        this(
                perfil.getId_perfil_marca(),
                perfil.getUsuarioId(), // CORREÇÃO: Pega o ID direto, pois não temos mais o objeto Usuario
                perfil.getProduto(),
                perfil.getTempoMercado(),
                perfil.getAtletasPatrocinados(),
                perfil.getTipoInvestimento(),
                perfil.getRedesSocial()
        );
    }
}