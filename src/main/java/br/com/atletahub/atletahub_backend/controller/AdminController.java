package br.com.atletahub.atletahub_backend.controller;

import br.com.atletahub.atletahub_backend.dto.admin.AdminDtos;
import br.com.atletahub.atletahub_backend.enums.StatusDenuncia;
import br.com.atletahub.atletahub_backend.model.StatusConta;
import br.com.atletahub.atletahub_backend.model.TipoUsuario;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Painel admin. Todas as rotas exigem a X-Admin-Key (AdminAccessFilter) e o papel ADMIN (SecurityConfig).
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/resumo")
    public Map<String, Object> resumo() {
        return adminService.resumo();
    }

    // ---------- Usuários ----------
    @GetMapping("/usuarios")
    public Map<String, Object> listarUsuarios(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) TipoUsuario tipo,
            @RequestParam(required = false) StatusConta status,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return adminService.listarUsuarios(busca, tipo, status, pagina, tamanho);
    }

    @GetMapping("/usuarios/{id}")
    public Map<String, Object> detalharUsuario(@PathVariable Long id) {
        return adminService.detalharUsuario(id);
    }

    @PostMapping("/usuarios/{id}/suspender")
    public ResponseEntity<Map<String, String>> suspender(
            @PathVariable Long id,
            @RequestBody @Valid AdminDtos.Suspensao corpo,
            @AuthenticationPrincipal Usuario admin) {
        adminService.suspender(admin.getIdUsuario(), id, corpo.motivo());
        return ResponseEntity.ok(Map.of("message", "Conta suspensa."));
    }

    @PostMapping("/usuarios/{id}/reativar")
    public ResponseEntity<Map<String, String>> reativar(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario admin) {
        adminService.reativar(admin.getIdUsuario(), id);
        return ResponseEntity.ok(Map.of("message", "Conta reativada."));
    }

    @PostMapping("/usuarios/{id}/encerrar")
    public ResponseEntity<Map<String, String>> encerrar(
            @PathVariable Long id,
            @RequestBody @Valid AdminDtos.Encerramento corpo,
            @AuthenticationPrincipal Usuario admin) {
        adminService.encerrar(admin.getIdUsuario(), id, corpo.confirmarEmail());
        return ResponseEntity.ok(Map.of("message", "Conta encerrada e anonimizada."));
    }

    @PostMapping("/usuarios/{id}/midia/remover")
    public ResponseEntity<Map<String, String>> removerMidia(
            @PathVariable Long id,
            @RequestBody @Valid AdminDtos.RemocaoMidia corpo) {
        adminService.removerMidia(id, corpo.url());
        return ResponseEntity.ok(Map.of("message", "Mídia removida da vitrine."));
    }

    // ---------- Denúncias ----------
    @GetMapping("/denuncias")
    public Map<String, Object> listarDenuncias(
            @RequestParam(required = false) StatusDenuncia status,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return adminService.listarDenuncias(status, pagina, tamanho);
    }

    @GetMapping("/denuncias/{id}")
    public Map<String, Object> detalharDenuncia(@PathVariable Long id) {
        return adminService.detalharDenuncia(id);
    }

    @PostMapping("/denuncias/{id}/resolver")
    public ResponseEntity<Map<String, String>> resolver(
            @PathVariable Long id,
            @RequestBody @Valid AdminDtos.Resolucao corpo,
            @AuthenticationPrincipal Usuario admin) {
        adminService.resolverDenuncia(admin.getIdUsuario(), id, corpo.decisao(), corpo.nota(),
                corpo.suspenderDenunciado(), corpo.motivoSuspensao());
        return ResponseEntity.ok(Map.of("message", "Denúncia analisada."));
    }
}
