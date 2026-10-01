package br.com.atletahub.atletahub_backend.dto.perfil;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record DadosAtualizacaoPerfilMarca(
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,
        @Email(message = "Email inválido")
        @Size(max = 100, message = "O email deve ter no máximo 100 caracteres")
        String email,
        @Size(max = 255, message = "O produto deve ter no máximo 255 caracteres")
        String produto,
        @Min(value = 0, message = "Tempo de mercado inválido")
        @Max(value = 500, message = "Tempo de mercado inválido")
        Integer tempoMercado,
        @Size(max = 5000, message = "Atletas patrocinados: texto muito longo")
        String atletasPatrocinados,
        @Size(max = 255, message = "O tipo de investimento deve ter no máximo 255 caracteres")
        String tipoInvestimento,
        @Size(max = 255, message = "Redes sociais deve ter no máximo 255 caracteres")
        String redesSocial,
        @Size(max = 500, message = "A URL da logo deve ter no máximo 500 caracteres")
        String logoUrl
) {}
