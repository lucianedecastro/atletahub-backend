package br.com.atletahub.atletahub_backend.traducao.provider.aws;

import br.com.atletahub.atletahub_backend.traducao.provider.TraducaoProvider;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.TranslateTextRequest;
import software.amazon.awssdk.services.translate.model.TranslateTextResponse;

/**
 * Provider de tradução usando AWS Translate.
 *
 * Implementa a interface TraducaoProvider para permitir
 * troca futura por outros serviços (OpenAI, Google, etc).
 */
@Component
public class AwsTraducaoProvider implements TraducaoProvider {

    private final TranslateClient translateClient;

    public AwsTraducaoProvider(TranslateClient translateClient) {
        this.translateClient = translateClient;
    }

    @Override
    public String traduzir(
            String texto,
            String idiomaOrigem,
            String idiomaDestino
    ) {

        TranslateTextRequest request = TranslateTextRequest.builder()
                .text(texto)
                .sourceLanguageCode(idiomaOrigem)
                .targetLanguageCode(idiomaDestino)
                .build();

        TranslateTextResponse response =
                translateClient.translateText(request);

        return response.translatedText();
    }
}

