package br.com.atletahub.atletahub_backend.traducao.provider.aws;

import br.com.atletahub.atletahub_backend.traducao.provider.TraducaoProvider;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.TranslateTextRequest;
import software.amazon.awssdk.services.translate.model.TranslateTextResponse;

@Component
public class AwsTraducaoProvider implements TraducaoProvider {

    private final TranslateClient translateClient;

    public AwsTraducaoProvider(TranslateClient translateClient) {
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

    // ⭐ NOVO MÉTODO: Tradução automática inteligente
    public String traduzirAutomaticamente(String texto, String idiomaPreferidoUsuario) {
        // 1. Se idioma preferido for configurado, usa como destino
        String idiomaDestino = (idiomaPreferidoUsuario != null && !idiomaPreferidoUsuario.isBlank())
                ? idiomaPreferidoUsuario
                : "en"; // Fallback padrão

        // 2. Detecta idioma de origem
        String idiomaOrigem = detectarIdiomaOuUsarAuto(texto);

        // 3. Se origem e destino forem iguais, inverte
        if (idiomaOrigem.equals(idiomaDestino)) {
            idiomaDestino = idiomaOrigem.equals("pt") ? "en" : "pt";
        }

        // 4. Traduz
        return traduzir(texto, idiomaOrigem, idiomaDestino);
    }

    private String detectarIdiomaOuUsarAuto(String texto) {
        // Lógica simples de detecção local
        if (contemCaracteresPortugueses(texto)) {
            return "pt";
        } else if (pareceIngles(texto)) {
            return "en";
        } else {
            return "auto"; // AWS detecta
        }
    }

    private boolean contemCaracteresPortugueses(String texto) {
        return texto.matches(".*[áàâãéèêíïóôõöúçñÁÀÂÃÉÈÊÍÏÓÔÕÖÚÇÑ].*");
    }

    private boolean pareceIngles(String texto) {
        String textoLower = texto.toLowerCase();
        String[] palavrasEn = {"hello", "hi", "how", "are", "you", "thanks", "thank"};
        for (String palavra : palavrasEn) {
            if (textoLower.contains(palavra)) return true;
        }
        return false;
    }
}