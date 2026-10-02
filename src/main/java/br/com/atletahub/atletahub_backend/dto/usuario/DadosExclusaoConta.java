package br.com.atletahub.atletahub_backend.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DadosExclusaoConta(
        @NotBlank(message = "Digite o e-mail da sua conta.")
        @Size(max = 100, message = "E-mail muito longo.")
        String email,

        @NotBlank(message = "Digite a sua senha.")
        @Size(max = 200, message = "Senha muito longa.")
        String senha
) {}
