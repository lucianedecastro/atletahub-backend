package br.com.atletahub.atletahub_backend.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record DadosLogin(
        @NotBlank(message = "O email é obrigatório para login.")
        @Email(message = "Formato de email inválido para login.")
        String email,

        @NotBlank(message = "A senha é obrigatória para login.")
        String senha
) {}
