package br.com.atletahub.atletahub_backend.traducao.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.translate.TranslateClient;

import java.time.Duration;

@Configuration
public class AwsTranslateConfig {

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    @Bean
    public TranslateClient translateClient() {
        return TranslateClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .httpClient(UrlConnectionHttpClient.create())
                // Timeout explícito: sem isso uma AWS lenta segura a conexão do banco por até ~30s.
                .overrideConfiguration(config -> config
                        .apiCallTimeout(Duration.ofSeconds(8))
                        .apiCallAttemptTimeout(Duration.ofSeconds(4)))
                .build();
    }
}
