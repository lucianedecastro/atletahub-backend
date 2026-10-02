package br.com.atletahub.atletahub_backend.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DadosRedefinirSenha(
        @NotBlank(message = "Link inválido ou expirado.")
        @Size(max = 100, message = "Link inválido ou expirado.")
        String token,

        @NotBlank(message = "Informe a nova senha.")
        @Size(max = 200, message = "Senha muito longa.")
        String novaSenha
) {}
