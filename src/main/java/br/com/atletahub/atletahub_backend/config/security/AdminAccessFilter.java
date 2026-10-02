package br.com.atletahub.atletahub_backend.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Primeira porta de toda rota /admin: exige o cabeçalho X-Admin-Key igual à variável de ambiente ADMIN_API_KEY.
 * - Sem a variável (ou com menos de 24 caracteres), o painel admin fica DESLIGADO: toda rota /admin responde 404.
 * - Chave errada também responde 404 (não confirma que a rota existe).
 * Só depois disso vêm o login (e-mail e senha de ADMIN) e o papel ADMIN do token.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AdminAccessFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AdminAccessFilter.class);
    static final int TAMANHO_MINIMO_DA_CHAVE = 24;

    @Value("${ADMIN_API_KEY:}")
    private String chaveConfigurada;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        boolean ehAdmin = uri.equals("/admin") || uri.startsWith("/admin/");
        // Pré-voo do CORS não leva cabeçalhos personalizados.
        return !ehAdmin || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (chaveConfigurada == null || chaveConfigurada.length() < TAMANHO_MINIMO_DA_CHAVE) {
            negar(response);
            return;
        }

        String enviada = request.getHeader("X-Admin-Key");
        boolean ok = enviada != null && MessageDigest.isEqual(
                enviada.getBytes(StandardCharsets.UTF_8),
                chaveConfigurada.getBytes(StandardCharsets.UTF_8));

        if (!ok) {
            logger.warn("Acesso a /admin recusado: chave ausente ou incorreta");
            negar(response);
            return;
        }

        chain.doFilter(request, response);
    }

    private static void negar(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\":\"Recurso não encontrado.\"}");
    }
}
