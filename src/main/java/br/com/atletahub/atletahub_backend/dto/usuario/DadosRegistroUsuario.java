package br.com.atletahub.atletahub_backend.dto.usuario;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DadosRegistroUsuario(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "Email inválido")
        @Size(max = 100, message = "O email deve ter no máximo 100 caracteres")
        String email,

        // Mínimo de 8 caracteres. O máximo de 72 é o limite do BCrypt (acima disso ele ignora o resto).
        // A regra vale só para NOVOS cadastros; quem já tem conta continua entrando normalmente.
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
        String senha,

        // ADMIN NÃO pode ser escolhido no cadastro público.
        // Contas de administrador devem ser criadas direto no banco.
        @NotBlank(message = "O tipo de usuário é obrigatório")
        @Pattern(
                regexp = "ATLETA|MARCA",
                message = "Tipo de usuário inválido"
        )
        String tipoUsuario,

        @NotBlank(message = "A cidade é obrigatória")
        @Size(max = 100, message = "A cidade deve ter no máximo 100 caracteres")
        String cidade,

        @NotBlank(message = "O estado é obrigatório")
        @Size(max = 100, message = "O estado deve ter no máximo 100 caracteres")
        String estado,

        // --- NOVO CAMPO: IDIOMA DE PREFERÊNCIA ---
        // Ex: "pt", "en", "es". O Service trata o padrão se vier nulo.
        @Size(max = 10, message = "Idioma inválido")
        String idioma,

        @AssertTrue(message = "É obrigatório concordar com os Termos de Uso e Política de Privacidade")
        Boolean concordoTermos

) {
}
