package br.com.atletahub.atletahub_backend.dto.usuario;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

// Usado por contas antigas, que ainda não informaram a data de nascimento.
public record DadosDataNascimento(
        @NotNull(message = "A data de nascimento é obrigatória")
        @Past(message = "Data de nascimento inválida")
        LocalDate dataNascimento
) {
}
