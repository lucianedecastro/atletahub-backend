package br.com.atletahub.atletahub_backend.traducao.metric;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class TraducaoMetricService {

    /**
     * Registra métricas básicas de tradução.
     *
     * @param idiomaOrigem idioma de origem
     * @param idiomaDestino idioma de destino
     * @param quantidadeCaracteres número de caracteres traduzidos
     * @param inicio instante de início da tradução
     */
    public void registrarTraducao(
            String idiomaOrigem,
            String idiomaDestino,
            int quantidadeCaracteres,
            Instant inicio
    ) {

        long tempoMs = Duration.between(inicio, Instant.now()).toMillis();

        // Por enquanto: log estruturado
        // Futuro: Micrometer / Prometheus / CloudWatch
        System.out.println(
                "[METRICA_TRADUCAO] " +
                        "origem=" + idiomaOrigem +
                        ", destino=" + idiomaDestino +
                        ", caracteres=" + quantidadeCaracteres +
                        ", tempoMs=" + tempoMs
        );
    }
}

