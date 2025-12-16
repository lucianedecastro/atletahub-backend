package br.com.atletahub.atletahub_backend.dto.perfil;

import jakarta.validation.constraints.Size;

public record DadosAtualizacaoPerfilMarca(
        String nome,   // <--- NOVO
        String email,  // <--- NOVO
        @Size(max = 255)
        String produto,
        Integer tempoMercado,
        String atletasPatrocinados,
        @Size(max = 255)
        String tipoInvestimento,
        String redesSocial,
        @Size(max = 500)
        String logoUrl
) {}