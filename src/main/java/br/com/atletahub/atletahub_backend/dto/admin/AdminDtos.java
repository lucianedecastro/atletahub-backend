package br.com.atletahub.atletahub_backend.dto.admin;

import br.com.atletahub.atletahub_backend.enums.StatusDenuncia;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class AdminDtos {

    private AdminDtos() {
    }

    public record Login(
            @NotBlank @Email String email,
            @NotBlank @Size(max = 72) String senha) {
    }

    public record Suspensao(
            @NotBlank(message = "Informe o motivo da suspensão")
            @Size(max = 500, message = "O motivo deve ter no máximo 500 caracteres")
            String motivo) {
    }

    // Para encerrar uma conta o admin digita o e-mail dela: evita clicar na linha errada.
    public record Encerramento(
            @NotBlank(message = "Digite o e-mail da conta para confirmar") String confirmarEmail) {
    }

    public record RemocaoMidia(
            @NotBlank(message = "Informe a mídia") @Size(max = 1000) String url) {
    }

    public record Resolucao(
            @NotNull(message = "Informe a decisão") StatusDenuncia decisao,
            @Size(max = 1000, message = "A nota deve ter no máximo 1000 caracteres") String nota,
            boolean suspenderDenunciado,
            @Size(max = 500) String motivoSuspensao) {
    }
}
