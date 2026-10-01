package br.com.atletahub.atletahub_backend.config;

import br.com.atletahub.atletahub_backend.config.security.SecurityFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Origens permitidas no CORS, separadas por vírgula (propriedade app.cors.allowed-origins).
    // Web: https://atleta-hub.vercel.app | App Capacitor: capacitor://localhost (iOS) e https://localhost (Android)
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, SecurityFilter securityFilter) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Sem token válido em rota protegida -> 401 (o app mobile usa isso para renovar a sessão).
                // Token válido sem permissão -> 403.
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, e) ->
                                responderErro(response, HttpServletResponse.SC_UNAUTHORIZED,
                                        "Sessão inválida ou expirada. Faça login novamente."))
                        .accessDeniedHandler((request, response, e) ->
                                responderErro(response, HttpServletResponse.SC_FORBIDDEN,
                                        "Você não tem permissão para acessar este recurso."))
                )

                .authorizeHttpRequests(authorize -> authorize

                        // 🔓 Health check (cold start / monitoramento)
                        .requestMatchers(HttpMethod.GET, "/health").permitAll()

                        // 🔓 Auth
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/registrar").permitAll()

                        // 🔓 Públicos
                        .requestMatchers(HttpMethod.GET, "/modalidades").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Perfis
                        .requestMatchers(HttpMethod.GET, "/perfil/atleta").hasRole("ATLETA")
                        .requestMatchers(HttpMethod.GET, "/perfil/marca").hasRole("MARCA")
                        .requestMatchers(HttpMethod.PUT, "/perfil/atleta").hasRole("ATLETA")
                        .requestMatchers(HttpMethod.PUT, "/perfil/marca").hasRole("MARCA")

                        // Usuários
                        // Listar TODOS os usuários (com e-mail) é só para ADMIN.
                        .requestMatchers(HttpMethod.GET, "/usuarios").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/usuarios/tipo").hasAnyRole("ATLETA", "MARCA", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/usuarios/**").authenticated()

                        // Interesses
                        .requestMatchers(HttpMethod.POST, "/interesses").authenticated()
                        .requestMatchers(HttpMethod.GET, "/interesses/enviados").authenticated()
                        .requestMatchers(HttpMethod.GET, "/interesses/recebidos").authenticated()

                        // Matches e mensagens
                        .requestMatchers(HttpMethod.GET, "/matches").authenticated()
                        .requestMatchers(HttpMethod.POST, "/mensagens").authenticated()
                        .requestMatchers(HttpMethod.GET, "/mensagens/match/{idMatch}").authenticated()

                        // Swagger (desligado por padrão em produção: ver springdoc.* no application.properties)
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> origens = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origem -> !origem.isEmpty())
                .toList();

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(origens);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        // A autenticação é por header Authorization (Bearer), não por cookie: não precisa de credenciais.
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private static void responderErro(HttpServletResponse response, int status, String mensagem) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\":\"" + mensagem + "\"}");
    }
}
