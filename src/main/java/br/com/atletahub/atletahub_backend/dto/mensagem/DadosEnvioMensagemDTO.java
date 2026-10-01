package br.com.atletahub.atletahub_backend.dto.mensagem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * O remetente NÃO vem mais no corpo da requisição: ele é o usuário dono do token.
 * Se o front antigo ainda mandar "idRemetente", o campo é simplesmente ignorado.
 */
public record DadosEnvioMensagemDTO(
        @NotNull(message = "O ID do match é obrigatório.")
        Long idMatch,
        @NotBlank(message = "O texto da mensagem não pode ser vazio.")
        @Size(max = 1000, message = "O texto da mensagem não pode exceder 1000 caracteres.")
        String texto
) {
}
