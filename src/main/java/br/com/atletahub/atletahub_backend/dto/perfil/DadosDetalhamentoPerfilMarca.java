package br.com.atletahub.atletahub_backend.dto.perfil;

import br.com.atletahub.atletahub_backend.model.PerfilMarca;

public record DadosDetalhamentoPerfilMarca(
        Long idPerfilMarca,
        Long idUsuario,
        String produto,
        Integer tempoMercado,
        String atletasPatrocinados,
        String tipoInvestimento,
        String redesSocial,
        // --- NOVO CAMPO (Para o Frontend conseguir ler a imagem) ---
        String logoUrl
) {
    public DadosDetalhamentoPerfilMarca(PerfilMarca perfil) {
        this(
                perfil.getId_perfil_marca(),
                perfil.getUsuarioId(),
                perfil.getProduto(),
                perfil.getTempoMercado(),
                perfil.getAtletasPatrocinados(),
                perfil.getTipoInvestimento(),
                perfil.getRedesSocial(),
                perfil.getLogoUrl() // Mapeando o novo campo
        );
    }
}