package br.com.atletahub.atletahub_backend.dto.perfil;

import jakarta.validation.constraints.Size;


public record DadosAtualizacaoPerfilMarca(
        @Size(max = 255)
        String produto,
        Integer tempoMercado,
        String atletasPatrocinados,
        @Size(max = 255)
        String tipoInvestimento,
        String redesSocial
) {}
