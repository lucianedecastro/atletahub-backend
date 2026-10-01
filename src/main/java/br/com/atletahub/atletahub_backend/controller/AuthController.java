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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
        // LGPD: o e-mail não vai mais para o log.
        logger.debug("Tentativa de login recebida");

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

        } catch (BadCredentialsException e) {
            // Credencial errada = 401 (antes devolvia 400).
            logger.info("Login recusado: credenciais inválidas");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Email ou senha inválidos"));

        } catch (Exception e) {
            // Falha de infraestrutura (ex.: banco acordando) NÃO é "senha errada".
            logger.error("Erro inesperado no login", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Não foi possível entrar agora. Tente novamente em instantes."));
        }
    }

    // ==========================
    // REGISTRO
    // ==========================
    @PostMapping({"/registrar", "/register"})
    public ResponseEntity<?> registrar(@RequestBody @Valid DadosRegistroUsuario dados) {
        logger.info("Registro solicitado (tipo={})", dados.tipoUsuario());

        try {
            usuarioService.registrarUsuario(dados);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Usuário registrado com sucesso"));

        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação no registro: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));

        } catch (DataIntegrityViolationException e) {
            // Dois cadastros simultâneos com o mesmo e-mail: o banco barra o segundo.
            logger.warn("Cadastro barrado por violação de unicidade");
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Email já cadastrado."));

        } catch (Exception e) {
            logger.error("Erro interno no registro", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erro interno ao realizar cadastro"));
        }
    }
}
