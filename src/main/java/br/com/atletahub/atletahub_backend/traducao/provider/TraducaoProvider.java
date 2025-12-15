package br.com.atletahub.atletahub_backend.traducao.provider;

/**
 * Contrato genérico para serviços de tradução.
 *
 * Implementações concretas podem utilizar AWS, OpenAI,
 * Google Translate ou qualquer outro provider externo.
 */
public interface TraducaoProvider {

    /**
     * Traduz um texto de um idioma para outro.
     *
     * @param texto texto original a ser traduzido
     * @param idiomaOrigem código do idioma de origem (ex: "pt", "en", "es", "auto")
     * @param idiomaDestino código do idioma de destino (ex: "en", "pt")
     * @return texto traduzido
     */
    String traduzir(String texto, String idiomaOrigem, String idiomaDestino);

}

