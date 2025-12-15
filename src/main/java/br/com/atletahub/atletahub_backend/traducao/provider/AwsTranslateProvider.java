package br.com.atletahub.atletahub_backend.traducao.provider;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.TranslateTextRequest;
import software.amazon.awssdk.services.translate.model.TranslateTextResponse;

/**
 * Implementação do TraducaoProvider utilizando AWS Translate.
 */
@Component
public class AwsTranslateProvider implements TraducaoProvider {

    private final TranslateClient translateClient;

    public AwsTranslateProvider(TranslateClient translateClient) {
        this.translateClient = translateClient;
    }

    @Override
    public String traduzir(String texto, String idiomaOrigem, String idiomaDestino) {

        TranslateTextRequest request = TranslateTextRequest.builder()
                .text(texto)
                .sourceLanguageCode(idiomaOrigem)
                .targetLanguageCode(idiomaDestino)
                .build();

        TranslateTextResponse response = translateClient.translateText(request);

        return response.translatedText();
    }
}

