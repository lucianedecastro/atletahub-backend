package br.com.atletahub.atletahub_backend.traducao.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "traducao")
public class TraducaoProperties {

    /**
     * Feature flag para ativar/desativar tradução automática.
     */
    private boolean automatica;

    /**
     * Idioma padrão de destino quando não informado.
     */
    private String idiomaPadraoDestino;

    // ===== GETTERS & SETTERS =====

    public boolean isAutomatica() {
        return automatica;
    }

    public void setAutomatica(boolean automatica) {
        this.automatica = automatica;
    }

    public String getIdiomaPadraoDestino() {
        return idiomaPadraoDestino;
    }

    public void setIdiomaPadraoDestino(String idiomaPadraoDestino) {
        this.idiomaPadraoDestino = idiomaPadraoDestino;
    }
}
