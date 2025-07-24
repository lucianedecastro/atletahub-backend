package br.com.atletahub.atletahub_backend.config;

import br.com.atletahub.atletahub_backend.config.security.SecurityFilter;
import org.springframework.beans.factory.annotation.Autowired;
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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Rotas de autenticação (login e registro) são PÚBLICAS
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/registrar").permitAll()

                        // Rotas de perfil para visualização (acessível apenas para o dono do perfil)
                        .requestMatchers(HttpMethod.GET, "/perfil/atleta").hasRole("ATLETA")
                        .requestMatchers(HttpMethod.GET, "/perfil/marca").hasRole("MARCA")

                        // Rotas para atualização do próprio perfil
                        .requestMatchers(HttpMethod.PUT, "/perfil/atleta").hasRole("ATLETA")
                        .requestMatchers(HttpMethod.PUT, "/perfil/marca").hasRole("MARCA")

                        // Rotas de listagem de usuários (dashboard) - acessível por usuários autenticados de qualquer tipo
                        .requestMatchers(HttpMethod.GET, "/usuarios/tipo").hasAnyRole("ATLETA", "MARCA", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/usuarios/{id}").authenticated()

                        // Rotas de interesse, match e mensagem (exigem autenticação)
                        .requestMatchers("/interesses").authenticated()
                        .requestMatchers("/matches").authenticated()
                        .requestMatchers("/mensagens").authenticated()
                        .requestMatchers("/mensagens/*").authenticated()

                        // O restante das rotas que não foram explicitamente permitidas exigem autenticação
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}