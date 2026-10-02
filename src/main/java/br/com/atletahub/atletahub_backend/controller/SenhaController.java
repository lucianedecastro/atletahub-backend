package br.com.atletahub.atletahub_backend.controller;

import br.com.atletahub.atletahub_backend.dto.usuario.DadosEsqueciSenha;
import br.com.atletahub.atletahub_backend.dto.usuario.DadosRedefinirSenha;
import br.com.atletahub.atletahub_backend.service.RecuperacaoSenhaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Rotas públicas da recuperação de senha (liberadas no SecurityConfig).
 */
@RestController
@RequestMapping("/auth")
public class SenhaController {

    private static final Logger logger = LoggerFactory.getLogger(SenhaController.class);

    // Mesma resposta exista ou não a conta: ninguém descobre quem tem cadastro.
    private static final String RESPOSTA_DO_PEDIDO =
            "Se esse e-mail tiver cadastro, enviamos um link para criar uma nova senha.";

    @Autowired
    private RecuperacaoSenhaService recuperacaoSenhaService;

    @PostMapping("/esqueci-senha")
    public ResponseEntity<?> esqueciSenha(@RequestBody @Valid DadosEsqueciSenha dados) {
        try {
            recuperacaoSenhaService.solicitar(dados.email());
            return ResponseEntity.ok(Map.of("message", RESPOSTA_DO_PEDIDO));
        } catch (Exception e) {
            logger.error("Erro ao solicitar redefinição de senha", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Não foi possível enviar agora. Tente novamente em instantes."));
        }
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<?> redefinirSenha(@RequestBody @Valid DadosRedefinirSenha dados) {
        try {
            recuperacaoSenhaService.redefinir(dados.token(), dados.novaSenha());
            return ResponseEntity.ok(Map.of("message", "Senha alterada. Agora você já pode entrar."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro ao redefinir senha", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Não foi possível alterar a senha agora. Tente novamente em instantes."));
        }
    }
}
