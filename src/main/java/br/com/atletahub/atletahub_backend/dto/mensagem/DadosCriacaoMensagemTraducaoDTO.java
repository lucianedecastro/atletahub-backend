package br.com.atletahub.atletahub_backend.dto.mensagem;

import jakarta.validation.constraints.NotNull;

public record DadosCriacaoMensagemTraducaoDTO(
        @NotNull
        Long idMensagem,

        // OPCIONAIS. O servidor detecta o idioma de origem sozinho (AWS "auto") e traduz para o
        // idioma de preferência de quem pediu. Os campos continuam aceitos só para não quebrar
        // versões antigas do app, que ainda os enviam.
        String idiomaOrigem,

        String idiomaDestino

        // Não existe campo de texto traduzido: ele é gerado pelo serviço de tradução.
) {
}
