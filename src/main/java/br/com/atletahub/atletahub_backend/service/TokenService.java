package br.com.atletahub.atletahub_backend.service;

import br.com.atletahub.atletahub_backend.model.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);
    private static final String ISSUER = "atletahub-api";

    @Value("${api.security.token.secret}")
    private String secret;

    // Duração do token em minutos (padrão: 2h, igual ao comportamento anterior).
    // Quando o refresh token entrar (Lote 3), este valor cai para ~15 min.
    @Value("${api.security.token.expiration-minutes:120}")
    private long expirationMinutes;

    private Algorithm algorithm;
    private JWTVerifier verifier;

    @PostConstruct
    void init() {
        if (secret == null || secret.length() < 32) {
            logger.warn("api.security.token.secret tem menos de 32 caracteres. "
                    + "Em produção use um segredo aleatório e longo.");
        }
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm).withIssuer(ISSUER).build();
    }

    public String generateToken(UserDetails userDetails) {
        Usuario usuario = (Usuario) userDetails;
        try {
            Instant agora = Instant.now();
            return JWT.create()
                    .withIssuer(ISSUER)
                    // O subject agora é o ID do usuário (e não o e-mail), para o token
                    // continuar válido caso o e-mail seja alterado no perfil.
                    .withSubject(String.valueOf(usuario.getIdUsuario()))
                    .withClaim("role", "ROLE_" + usuario.getTipoUsuario().name())
                    .withIssuedAt(agora)
                    // Instant.now() é sempre UTC: não depende do fuso do servidor.
                    .withExpiresAt(agora.plus(Duration.ofMinutes(expirationMinutes)))
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    /**
     * Valida assinatura, emissor e expiração. Devolve o subject (ID do usuário, em texto).
     * Lança JWTVerificationException se o token for inválido ou estiver expirado.
     */
    public String getSubject(String token) {
        return verifier.verify(token).getSubject();
    }
}
