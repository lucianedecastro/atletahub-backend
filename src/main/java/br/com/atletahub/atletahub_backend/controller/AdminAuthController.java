package br.com.atletahub.atletahub_backend.controller;

import br.com.atletahub.atletahub_backend.dto.admin.AdminDtos;
import br.com.atletahub.atletahub_backend.model.TipoUsuario;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.service.TokenService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Login do painel admin. Só chega aqui com a X-Admin-Key correta (AdminAccessFilter).
 * Aceita apenas contas ADMIN, dá um token curto e trava depois de várias falhas seguidas.
 */
@RestController
@RequestMapping("/admin/auth")
public class AdminAuthController {

    private static final Logger logger = LoggerFactory.getLogger(AdminAuthController.class);

    private static final int MAX_FALHAS = 5;
    private static final Duration TRAVA = Duration.ofMinutes(15);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    // Duração do token do admin: bem menor que a do usuário comum.
    @Value("${ADMIN_TOKEN_MINUTES:60}")
    private long minutosDoToken;

    // Contagem simples em memória (só o dono usa o painel; reiniciar o servidor zera).
    private int falhasSeguidas = 0;
    private Instant travadoAte = null;

    @PostMapping("/login")
    public synchronized ResponseEntity<?> login(@RequestBody @Valid AdminDtos.Login dados) {
        Instant agora = Instant.now();
        if (travadoAte != null && agora.isBefore(travadoAte)) {
            long minutos = Math.max(1, Duration.between(agora, travadoAte).toMinutes());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Muitas tentativas. Tente de novo em " + minutos + " min."));
        }

        try {
            var autenticacao = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dados.email(), dados.senha()));
            Usuario usuario = (Usuario) autenticacao.getPrincipal();

            if (usuario.getTipoUsuario() != TipoUsuario.ADMIN) {
                return falhar();
            }

            falhasSeguidas = 0;
            travadoAte = null;

            Map<String, Object> resposta = new LinkedHashMap<>();
            resposta.put("token", tokenService.generateToken(usuario, minutosDoToken));
            resposta.put("nome", usuario.getNome());
            resposta.put("expiraEmMinutos", minutosDoToken);
            logger.info("Login de administrador realizado");
            return ResponseEntity.ok(resposta);

        } catch (AuthenticationException e) {
            return falhar();
        }
    }

    private ResponseEntity<?> falhar() {
        falhasSeguidas++;
        if (falhasSeguidas >= MAX_FALHAS) {
            travadoAte = Instant.now().plus(TRAVA);
            falhasSeguidas = 0;
            logger.warn("Login de administrador travado por {} min após falhas seguidas", TRAVA.toMinutes());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "E-mail ou senha inválidos."));
    }
}
