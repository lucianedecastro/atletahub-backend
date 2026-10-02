package br.com.atletahub.atletahub_backend.controller;

import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.service.BloqueioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Bloqueio entre usuários. Todas as rotas exigem login (anyRequest().authenticated() no SecurityConfig).
 * Erros usam 404/409 (nunca 401/403: o site desloga a pessoa nesses dois).
 */
@RestController
@RequestMapping("/bloqueios")
public class BloqueioController {

    @Autowired
    private BloqueioService bloqueioService;

    @GetMapping
    public List<Map<String, Object>> listar(@AuthenticationPrincipal Usuario logado) {
        return bloqueioService.listar(logado);
    }

    @PostMapping("/{idUsuario}")
    public ResponseEntity<Map<String, String>> bloquear(
            @PathVariable Long idUsuario,
            @AuthenticationPrincipal Usuario logado) {
        bloqueioService.bloquear(logado, idUsuario);
        return ResponseEntity.ok(Map.of("message", "Pessoa bloqueada."));
    }

    @DeleteMapping("/{idUsuario}")
    public ResponseEntity<Map<String, String>> desbloquear(
            @PathVariable Long idUsuario,
            @AuthenticationPrincipal Usuario logado) {
        bloqueioService.desbloquear(logado, idUsuario);
        return ResponseEntity.ok(Map.of("message", "Pessoa desbloqueada."));
    }
}
