package br.com.atletahub.atletahub_backend.dto.usuario;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DadosRegistroUsuario(

        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        String senha,

        @NotBlank(message = "O tipo de usuário é obrigatório")
        @Pattern(
                regexp = "ATLETA|MARCA|ADMIN",
                message = "Tipo de usuário inválido"
        )
        String tipoUsuario,

        @NotBlank(message = "A cidade é obrigatória")
        String cidade,

        @NotBlank(message = "O estado é obrigatório")
        String estado,

        @AssertTrue(message = "É obrigatório concordar com os Termos de Uso e Política de Privacidade")
        Boolean concordoTermos

) {
}
