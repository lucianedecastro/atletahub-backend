package br.com.atletahub.atletahub_backend.dto.usuario;

import br.com.atletahub.atletahub_backend.model.TipoUsuario;
import jakarta.validation.constraints.*;

public record DadosRegistroUsuario(
        @NotBlank
        String nome,
        @NotBlank @Email
        String email,
        @NotBlank
        String senha,
        @NotBlank @Pattern(regexp = "ATLETA|MARCA|ADMIN")
        String tipoUsuario,
        @NotBlank
        String cidade,
        @NotBlank
        String estado
) {
}