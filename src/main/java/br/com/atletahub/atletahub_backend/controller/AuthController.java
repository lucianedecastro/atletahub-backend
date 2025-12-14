package br.com.atletahub.atletahub_backend.controller;

import br.com.atletahub.atletahub_backend.dto.usuario.DadosLogin;
import br.com.atletahub.atletahub_backend.dto.usuario.DadosRegistroUsuario;
import br.com.atletahub.atletahub_backend.model.TipoUsuario;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.repository.UsuarioRepository;
import br.com.atletahub.atletahub_backend.service.TokenService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid DadosLogin dados) {
        logger.info("Tentativa de login para: {}", dados.email());

        try {

            UsernamePasswordAuthenticationToken usernamePassword = new UsernamePasswordAuthenticationToken(dados.email(), dados.senha());

            Authentication auth = this.authenticationManager.authenticate(usernamePassword);


            Usuario usuarioLogado = (Usuario) auth.getPrincipal();


            String token = tokenService.generateToken(usuarioLogado);


            Map<String, Object> response = new HashMap<>();
            response.put("token", token);

            Map<String, String> userData = new HashMap<>();
            userData.put("id", usuarioLogado.getIdUsuario().toString());
            userData.put("email", usuarioLogado.getEmail());
            userData.put("name", usuarioLogado.getNome());
            userData.put("userType", usuarioLogado.getTipoUsuario().toString().toLowerCase());

            response.put("user", userData);

            logger.info("Login bem-sucedido para: {}", dados.email());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Erro no login para {}: {}", dados.email(), e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro no login: " + e.getMessage());
        }
    }

    @PostMapping("/registrar")
    public ResponseEntity registrar(@RequestBody @Valid DadosRegistroUsuario dados) {
        logger.info("Tentativa de registro para: {}", dados.email());

        if (this.usuarioRepository.findByEmail(dados.email()).isPresent()) {
            logger.warn("Email já cadastrado: {}", dados.email());
            return ResponseEntity.badRequest().body("Email já cadastrado.");
        }

        try {

            TipoUsuario tipoUsuarioEnum = TipoUsuario.valueOf(dados.tipoUsuario().toUpperCase());

            String senhaCriptografada = passwordEncoder.encode(dados.senha());


            Usuario novoUsuario = new Usuario(dados.nome(), dados.email(), senhaCriptografada, tipoUsuarioEnum);
            usuarioRepository.save(novoUsuario);

            logger.info("Registro bem-sucedido para: {}", dados.email());
            return ResponseEntity.ok("Usuário registrado com sucesso!");
        } catch (BadCredentialsException e) {
            logger.warn("Credenciais inválidas para: {}", dados.email());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Credenciais inválidas"));
        } catch (Exception e) {
            logger.error("Erro no login para {}: {}", dados.email(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Erro interno ao realizar login"));
        }
    }
}