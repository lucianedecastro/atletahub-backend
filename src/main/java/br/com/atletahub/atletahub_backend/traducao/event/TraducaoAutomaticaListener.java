package br.com.atletahub.atletahub_backend.traducao.event;

import br.com.atletahub.atletahub_backend.dto.mensagem.DadosCriacaoMensagemTraducaoDTO;
import br.com.atletahub.atletahub_backend.service.MensagemTraducaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Faz a tradução automática DEPOIS que a mensagem foi gravada (commit) e em outra thread.
 *
 * Por que assim:
 *  - Antes, a tradução rodava dentro da mesma transação do envio. Se a AWS falhasse, a transação
 *    ficava marcada como "rollback-only" e a mensagem inteira se perdia (erro 500 para quem enviou).
 *  - Agora a mensagem já está salva; se a tradução falhar, só sobra um aviso no log.
 *    O botão "Traduzir" do chat continua funcionando (traduz na hora e guarda em cache).
 */
@Component
public class TraducaoAutomaticaListener {

    private static final Logger logger = LoggerFactory.getLogger(TraducaoAutomaticaListener.class);

    private final MensagemTraducaoService mensagemTraducaoService;

    public TraducaoAutomaticaListener(MensagemTraducaoService mensagemTraducaoService) {
        this.mensagemTraducaoService = mensagemTraducaoService;
    }

    @Async("traducaoExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void traduzirAposEnvio(MensagemEnviadaEvent evento) {
        try {
            mensagemTraducaoService.traduzirMensagemInterno(new DadosCriacaoMensagemTraducaoDTO(
                    evento.idMensagem(),
                    evento.idiomaOrigem(),
                    evento.idiomaDestino()
            ));
        } catch (Exception e) {
            logger.warn("Tradução automática falhou para a mensagem {}: {}", evento.idMensagem(), e.getMessage());
        }
    }
}
