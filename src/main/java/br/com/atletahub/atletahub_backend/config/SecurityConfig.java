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
import java.util.Collections; // Importante para o singletonList ou listas vazias se precisar

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, SecurityFilter securityFilter) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Configura o CORS aqui
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize

                        // Rotas públicas (Auth)
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        // CORREÇÃO CRÍTICA: Adicionado /register (inglês) para bater com o Frontend
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/registrar").permitAll() // Mantém o PT-BR por compatibilidade

                        // Rotas públicas (Gerais)
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

                        // Swagger (documentação pública)
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Qualquer outra rota exige autenticação
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

        // CORREÇÃO DE CORS: "allowedOriginPatterns" é mais flexível que "allowedOrigins"
        // O "*" aqui diz: Aceite conexões de QUALQUER lugar (www.atletahub, localhost, render, etc)
        // Isso resolve o problema de origens bloqueadas durante o desenvolvimento/testes.
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        configuration.setAllowCredentials(true); // Permite enviar cookies/tokens

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}