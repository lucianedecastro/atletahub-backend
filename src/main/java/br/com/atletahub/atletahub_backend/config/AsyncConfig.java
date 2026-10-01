package br.com.atletahub.atletahub_backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    /**
     * Pool pequeno e LIMITADO para a tradução automática.
     * O banco gratuito tem poucas conexões (Hikari = 5) e o Render free tem pouca memória:
     * no máximo 2 traduções ao mesmo tempo e 50 na fila. Se a fila encher, a tradução automática
     * daquela mensagem é descartada (o envio da mensagem NÃO é afetado; o botão "Traduzir" segue funcionando).
     */
    @Bean(name = "traducaoExecutor")
    public Executor traducaoExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("traducao-");
        executor.setRejectedExecutionHandler((tarefa, pool) ->
                logger.warn("Fila de tradução automática cheia: tradução descartada."));
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        return executor;
    }
}
