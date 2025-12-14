package br.com.atletahub.atletahub_backend.dto.mensagem;

import br.com.atletahub.atletahub_backend.model.Mensagem;

import java.time.LocalDateTime;

public record DetalhesMensagemDTO(
        Long id,
        Long idMatch,
        Long idRemetente,
        String texto,
        LocalDateTime dataEnvio
) {
    public DetalhesMensagemDTO(Mensagem mensagem) {
        this(mensagem.getId(), mensagem.getMatch().getId(), mensagem.getRemetente().getIdUsuario(), mensagem.getTexto(), mensagem.getDataEnvio());
    }
}
