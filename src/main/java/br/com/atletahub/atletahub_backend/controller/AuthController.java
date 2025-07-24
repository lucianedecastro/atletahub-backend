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
    private PasswordEncoder passwordEncoder; // Para o método de registro
    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid DadosLogin dados) {
        logger.info("Tentativa de login para: {}", dados.email());

        try {
            // Cria um objeto de autenticação com as credenciais fornecidas
            UsernamePasswordAuthenticationToken usernamePassword = new UsernamePasswordAuthenticationToken(dados.email(), dados.senha());
            // Autentica o usuário usando o AuthenticationManager
            Authentication auth = this.authenticationManager.authenticate(usernamePassword);

            // Obtém o usuário autenticado
            Usuario usuarioLogado = (Usuario) auth.getPrincipal();

            // Gera o token JWT para o usuário autenticado
            String token = tokenService.generateToken(usuarioLogado);

            // Prepara a resposta incluindo o token e os dados do usuário
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);

            Map<String, String> userData = new HashMap<>();
            userData.put("id", usuarioLogado.getIdUsuario().toString());
            userData.put("email", usuarioLogado.getEmail());
            userData.put("name", usuarioLogado.getNome());
            userData.put("userType", usuarioLogado.getTipoUsuario().toString().toLowerCase()); // Garante o tipo em minúsculas

            response.put("user", userData); // Adiciona o objeto 'user' à resposta

            logger.info("Login bem-sucedido para: {}", dados.email());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Erro no login para {}: {}", dados.email(), e.getMessage());
            // Retorna uma resposta de erro mais específica, se possível
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
            // Converte a String tipoUsuario para o Enum TipoUsuario
            TipoUsuario tipoUsuarioEnum = TipoUsuario.valueOf(dados.tipoUsuario().toUpperCase());

            String senhaCriptografada = passwordEncoder.encode(dados.senha());


            Usuario novoUsuario = new Usuario(dados.nome(), dados.email(), senhaCriptografada, tipoUsuarioEnum);
            usuarioRepository.save(novoUsuario);

            logger.info("Registro bem-sucedido para: {}", dados.email());
            return ResponseEntity.ok("Usuário registrado com sucesso!");
        } catch (IllegalArgumentException e) {
            logger.error("Tipo de usuário inválido: {}. Erro: {}", dados.tipoUsuario(), e.getMessage());
            return ResponseEntity.badRequest().body("Tipo de usuário inválido.");
        } catch (Exception e) {
            logger.error("Erro no registro para {}: {}", dados.email(), e.getMessage());
            return ResponseEntity.internalServerError().body("Erro ao registrar usuário.");
        }
    }
}