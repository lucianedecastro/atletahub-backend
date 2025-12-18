package br.com.atletahub.atletahub_backend.config;

import br.com.atletahub.atletahub_backend.config.security.SecurityFilter;
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

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, SecurityFilter securityFilter) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
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

                        // Swagger
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
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
