package br.com.atletahub.config;

import br.com.atletahub.config.JwtRequestFilter; // Seu filtro JWT
import br.com.atletahub.service.UserDetailsServiceImpl; // Seu UserDetailsService

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer; // Importe esta classe!
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration; // Importe esta classe!
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // Importe esta classe!
import org.springframework.web.filter.CorsFilter; // Importe esta classe!


@Configuration
@EnableWebSecurity // Habilita a segurança web do Spring Security
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl; // Injeta o serviço de detalhes do usuário

    @Autowired
    private JwtRequestFilter jwtRequestFilter; // Injeta nosso filtro JWT

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configura o AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Configura o provedor de autenticação com o UserDetailsService e PasswordEncoder
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsServiceImpl);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Desabilita CSRF
                .cors(Customizer.withDefaults()) // Habilita CORS configurado pelo Bean corsFilter()
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll() // Permite acesso a endpoints de autenticação
                        .anyRequest().authenticated() // Todas as outras requisições exigem autenticação
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Configura o Spring Security para não criar sessões
                )
                .authenticationProvider(authenticationProvider()) // Adiciona nosso provedor de autenticação
                // Adiciona nosso filtro JWT antes do filtro de autenticação padrão de usuário/senha
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        // Se você não for usar autenticação HTTP Basic, remova ou comente esta linha
        // .httpBasic(Customizer.withDefaults()); // Remova se usar apenas JWT

        return http.build();
    }

    // NOVO BEAN: Configuração de CORS para Produção
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); // Permite o envio de credenciais (cookies, headers de autenticação)

        // **IMPORTANTE**: Em produção, esta deve ser a URL REAL do seu frontend!
        // Por enquanto, mantenha localhost para desenvolvimento.
        // Você pode ter múltiplas origens permitidas, basta adicionar mais `addAllowedOrigin()`
        config.addAllowedOrigin("http://localhost:3000"); // Para desenvolvimento local do frontend
        // config.addAllowedOrigin("https://sua-app-frontend.com"); // PARA PRODUÇÃO!

        config.addAllowedHeader("*"); // Permite todos os cabeçalhos
        config.addAllowedMethod("*"); // Permite todos os métodos (GET, POST, PUT, DELETE, etc.)
        source.registerCorsConfiguration("/**", config); // Aplica a configuração a todas as rotas
        return new CorsFilter(source);
    }
}