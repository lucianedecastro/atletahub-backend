package br.com.atletahub.atletahub_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Envio de e-mail pelo Resend (API HTTPS, sem biblioteca extra).
 * Configuração por variáveis de ambiente no Render:
 *   RESEND_API_KEY = chave da API do Resend
 *   MAIL_FROM      = remetente, por exemplo: AtletaHub <nao-responda@seudominio.com.br>
 * Sem as duas variáveis o envio fica DESLIGADO: nada quebra, só não sai e-mail (e o log avisa).
 * Para trocar de provedor no futuro, só esta classe muda.
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final String ENDERECO_DA_API = "https://api.resend.com/emails";

    @Value("${RESEND_API_KEY:}")
    private String chaveDaApi;

    @Value("${MAIL_FROM:}")
    private String remetente;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    // Fila pequena em segundo plano: a resposta ao usuário não espera o e-mail sair.
    private final ExecutorService executor = Executors.newFixedThreadPool(2, tarefa -> {
        Thread fio = new Thread(tarefa, "envio-de-email");
        fio.setDaemon(true);
        return fio;
    });

    public boolean estaAtivo() {
        return chaveDaApi != null && !chaveDaApi.isBlank() && remetente != null && !remetente.isBlank();
    }

    public void enviarEmSegundoPlano(String para, String assunto, String html, String texto) {
        if (!estaAtivo()) {
            logger.warn("Envio de e-mail desligado (faltam RESEND_API_KEY ou MAIL_FROM). Nenhum e-mail foi enviado.");
            return;
        }
        executor.submit(() -> enviar(para, assunto, html, texto));
    }

    private void enviar(String para, String assunto, String html, String texto) {
        try {
            String corpo = mapper.writeValueAsString(Map.of(
                    "from", remetente,
                    "to", List.of(para),
                    "subject", assunto,
                    "html", html,
                    "text", texto));

            HttpRequest requisicao = HttpRequest.newBuilder(URI.create(ENDERECO_DA_API))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Bearer " + chaveDaApi.trim())
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "AtletaHub-Backend")
                    .POST(HttpRequest.BodyPublishers.ofString(corpo))
                    .build();

            HttpResponse<String> resposta = http.send(requisicao, HttpResponse.BodyHandlers.ofString());

            if (resposta.statusCode() / 100 == 2) {
                logger.info("E-mail enviado ao provedor (status {})", resposta.statusCode());
            } else {
                String detalhe = resposta.body() == null ? "" : resposta.body();
                if (detalhe.length() > 300) {
                    detalhe = detalhe.substring(0, 300);
                }
                logger.error("Provedor de e-mail recusou o envio (status {}): {}", resposta.statusCode(), detalhe);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            // Sem o endereço do destinatário no log (LGPD).
            logger.error("Falha ao enviar e-mail: {}", e.getClass().getSimpleName());
        }
    }

    @PreDestroy
    void encerrar() {
        executor.shutdown();
    }
}
