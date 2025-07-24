package br.com.atletahub.atletahub_backend.dto.mensagem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DadosEnvioMensagemDTO(
        @NotNull(message = "O ID do match é obrigatório.")
        Long idMatch,
        @NotNull(message = "O ID do remetente é obrigatório.")
        Long idRemetente,
        @NotBlank(message = "O texto da mensagem não pode ser vazio.")
        @Size(max = 1000, message = "O texto da mensagem não pode exceder 1000 caracteres.")
        String texto
) {
}
