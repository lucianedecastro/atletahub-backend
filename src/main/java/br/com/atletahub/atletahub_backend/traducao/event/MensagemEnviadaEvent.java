package br.com.atletahub.atletahub_backend.traducao.event;

/**
 * Evento publicado quando uma mensagem é salva e os idiomas de remetente e destinatário
 * são diferentes. Carrega só IDs e códigos de idioma (nunca o texto da mensagem).
 */
public record MensagemEnviadaEvent(
        Long idMensagem,
        String idiomaOrigem,
        String idiomaDestino
) {
}
