package br.com.atletahub.atletahub_backend.traducao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.translate.TranslateClient;

/**
 * Configuração do cliente AWS Translate.
 *
 * Usa:
 * - Variáveis de ambiente em local
 * - IAM Role automaticamente em cloud (Render / EC2 / ECS)
 */
@Configuration
public class AwsTranslateConfig {

    @Bean
    public TranslateClient translateClient() {
        return TranslateClient.builder()
                .region(Region.US_EAST_1) // ajuste se necessário
                .build();
    }
}
