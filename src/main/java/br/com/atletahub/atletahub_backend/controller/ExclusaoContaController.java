package br.com.atletahub.atletahub_backend.controller;

import br.com.atletahub.atletahub_backend.dto.usuario.DadosExclusaoConta;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.service.ContaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * Exclusão da própria conta (direito do titular, LGPD). Exige login; a rota já cai em "authenticated"
 * no SecurityConfig. Usa POST (e não DELETE com corpo) porque alguns proxies descartam o corpo do DELETE.
 */
@RestController
@RequestMapping("/conta")
public class ExclusaoContaController {

    private static final Logger logger = LoggerFactory.getLogger(ExclusaoContaController.class);

    @Autowired
    private ContaService contaService;

    @PostMapping("/excluir")
    public ResponseEntity<?> excluir(
            @RequestBody @Valid DadosExclusaoConta dados,
            @AuthenticationPrincipal Usuario logado) {
        try {
            contaService.excluirPelaPropriaPessoa(logado.getIdUsuario(), dados.email(), dados.senha());
            return ResponseEntity.ok(Map.of("message", "Conta excluída."));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("message", e.getReason() == null ? "Não foi possível excluir a conta." : e.getReason()));
        } catch (Exception e) {
            logger.error("Erro ao excluir conta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Não foi possível excluir a conta agora. Tente novamente em instantes."));
        }
    }
}
