package br.com.atletahub.atletahub_backend.config.security;

import br.com.atletahub.atletahub_backend.repository.UsuarioRepository;
import br.com.atletahub.atletahub_backend.service.TokenService;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    public SecurityFilter(TokenService tokenService, UsuarioRepository usuarioRepository) {
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = recuperarToken(request);

        if (token != null) {
            try {
                // O subject do token é o ID do usuário.
                Long idUsuario = Long.valueOf(tokenService.getSubject(token));

                usuarioRepository.findById(idUsuario).ifPresent(usuario -> {
                    var authentication = new UsernamePasswordAuthenticationToken(
                            usuario,
                            null,
                            usuario.getAuthorities()
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });

            } catch (JWTVerificationException | NumberFormatException ex) {
                // Token inválido, expirado ou no formato antigo (subject = e-mail):
                // segue SEM autenticar. Rotas públicas (login/registro) continuam funcionando
                // mesmo se o front mandar um token velho; rotas protegidas respondem 401
                // pelo authenticationEntryPoint configurado no SecurityConfig.
                logger.debug("Token rejeitado: {}", ex.getClass().getSimpleName());
            }
            // Qualquer OUTRO erro (ex.: banco fora do ar / Neon acordando) NÃO é engolido:
            // vira 500 e não 401/403, para o app não deslogar o usuário por falha de infraestrutura.
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Ignora requisições OPTIONS (preflight CORS)
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    private String recuperarToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}
