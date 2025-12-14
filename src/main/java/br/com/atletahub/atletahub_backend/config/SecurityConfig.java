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
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize

                        // Rotas públicas
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/registrar").permitAll()
                        .requestMatchers(HttpMethod.GET, "/modalidades").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Perfis
                        .requestMatchers(HttpMethod.GET, "/perfil/atleta").hasRole("ATLETA")
                        .requestMatchers(HttpMethod.GET, "/perfil/marca").hasRole("MARCA")
                        .requestMatchers(HttpMethod.PUT, "/perfil/atleta").hasRole("ATLETA")
                        .requestMatchers(HttpMethod.PUT, "/perfil/marca").hasRole("MARCA")

                        // Usuários
                        .requestMatchers(HttpMethod.GET, "/usuarios/tipo").hasAnyRole("ATLETA", "MARCA", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/usuarios/**").authenticated() // Corrigido para aceitar paths com {id}

                        // Interesses
                        .requestMatchers(HttpMethod.POST, "/interesses").authenticated()
                        .requestMatchers(HttpMethod.GET, "/interesses/enviados").authenticated()
                        .requestMatchers(HttpMethod.GET, "/interesses/recebidos").authenticated()

                        // Matches e mensagens
                        .requestMatchers(HttpMethod.GET, "/matches").authenticated()
                        .requestMatchers(HttpMethod.POST, "/mensagens").authenticated()
                        .requestMatchers(HttpMethod.GET, "/mensagens/match/{idMatch}").authenticated()

                        // Swagger (documentação pública)
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Qualquer outra rota exige autenticação
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
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
        configuration.setAllowedOrigins(Arrays.asList(

                "http://localhost:5173",
                "http://localhost:8080",
                "https://atleta-hub.vercel.app",
                "https://api.atletahub.com.br",
                "https://www.atletahub.com.br"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
