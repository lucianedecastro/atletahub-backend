package br.com.atletahub.controller;

import br.com.atletahub.dto.AuthRequest;
import br.com.atletahub.dto.AuthResponse;
import br.com.atletahub.model.Usuario;
import br.com.atletahub.repository.UsuarioRepository;
import br.com.atletahub.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder; // Importe esta classe
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(409).body("E-mail já cadastrado.");
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(request.getNome());
        novoUsuario.setEmail(request.getEmail());
        novoUsuario.setTipoUsuario(request.getTipoUsuario());
        novoUsuario.setSenhaHash(passwordEncoder.encode(request.getSenha()));

        usuarioRepository.save(novoUsuario);

        return ResponseEntity.status(201).body("Usuário registrado com sucesso!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha())
            );

            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(request.getEmail());
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(401).body("Credenciais inválidas ou usuário não encontrado após autenticação.");
            }

            Usuario usuario = usuarioOpt.get();
            String token = jwtService.gerarToken(usuario);

            return ResponseEntity.ok(new AuthResponse(token));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Email ou senha inválidos.");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Usuário não encontrado no banco de dados.");
        }

        Usuario usuario = usuarioOpt.get();
        usuario.setSenhaHash(null); // Por segurança, não enviar a senha hash para o frontend
        return ResponseEntity.ok(usuario);
    }
}