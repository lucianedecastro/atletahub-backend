package br.com.atletahub.atletahub_backend.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DadosEsqueciSenha(
        @NotBlank(message = "Informe o e-mail da sua conta.")
        @Email(message = "Formato de e-mail inválido.")
        @Size(max = 100, message = "E-mail muito longo.")
        String email
) {}
