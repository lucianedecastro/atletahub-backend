package br.com.atletahub.atletahub_backend.dto.mensagem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DadosCriacaoMensagemTraducaoDTO(
        @NotNull
        Long idMensagem,

        @NotBlank
        String idiomaOrigem,

        @NotBlank
        String idiomaDestino

        // REMOVIDO: @NotBlank String textoTraduzido
        // Este campo será gerado pelo serviço de tradução
        // Não deve ser enviado pelo frontend
) {
}