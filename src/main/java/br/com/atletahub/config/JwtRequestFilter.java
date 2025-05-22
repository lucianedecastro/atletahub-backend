package br.com.atletahub.config;

import br.com.atletahub.service.JwtService;
import br.com.atletahub.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtService jwtService; // Certifique-se de que este serviço tem um método para validar tokens

    /**
     * Este método é chamado para cada requisição HTTP.
     * Ele inspeciona o cabeçalho 'Authorization' para um token JWT.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // Verifica se o cabeçalho Authorization existe e começa com "Bearer "
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // Extrai o token removendo "Bearer "
            try {
                username = jwtService.extractUsername(jwt); // Assume que JwtService tem este método
            } catch (Exception e) {
                // Em caso de token inválido ou expirado, o username será null
                logger.warn("JWT Token inválido ou expirado", e);
            }
        }

        // Se o username foi extraído e não há autenticação no contexto de segurança atual
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Carrega os detalhes do usuário usando o UserDetailsService
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Valida o token novamente com os detalhes do usuário
            // Certifique-se de que JwtService.validateToken() existe e faz a validação completa
            if (jwtService.validateToken(jwt, userDetails)) {

                // Cria um objeto de autenticação e o define no SecurityContextHolder
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response); // Continua a cadeia de filtros
    }
}
