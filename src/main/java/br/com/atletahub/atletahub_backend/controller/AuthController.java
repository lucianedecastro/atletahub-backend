package br.com.atletahub.atletahub_backend.controller;

import br.com.atletahub.atletahub_backend.dto.usuario.DadosLogin;
import br.com.atletahub.atletahub_backend.dto.usuario.DadosRegistroUsuario;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.service.TokenService;
import br.com.atletahub.atletahub_backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private TokenService tokenService;

    // ==========================
    // LOGIN
    // ==========================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid DadosLogin dados) {
        logger.info("Tentativa de login para: {}", dados.email());

        try {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(dados.email(), dados.senha());

            Authentication authentication = authenticationManager.authenticate(authToken);
            Usuario usuario = (Usuario) authentication.getPrincipal();

            String token = tokenService.generateToken(usuario);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);

            Map<String, Object> user = new HashMap<>();
            user.put("id", usuario.getIdUsuario());
            user.put("email", usuario.getEmail());
            user.put("name", usuario.getNome());
            user.put("userType", usuario.getTipoUsuario().name().toLowerCase());

            response.put("user", user);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Erro no login", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Email ou senha inválidos"));
        }
    }

    // ==========================
    // REGISTRO (CORRETO)
    // ==========================
    @PostMapping({"/registrar", "/register"})
    public ResponseEntity<?> registrar(@RequestBody @Valid DadosRegistroUsuario dados) {
        logger.info("Tentativa de registro para: {}", dados.email());

        try {
            usuarioService.registrarUsuario(dados);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Usuário registrado com sucesso"));

        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação no registro: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));

        } catch (Exception e) {
            logger.error("Erro interno no registro", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erro interno ao realizar cadastro"));
        }
    }
}
