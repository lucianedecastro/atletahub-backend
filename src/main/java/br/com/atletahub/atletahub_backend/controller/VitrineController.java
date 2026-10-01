package br.com.atletahub.atletahub_backend.controller;

import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.model.mongo.PerfilVitrine;
import br.com.atletahub.atletahub_backend.service.VitrineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/vitrine")
public class VitrineController {

    @Autowired
    private VitrineService vitrineService;

    // Endpoint para o próprio usuário (edição)
    @GetMapping("/me")
    public ResponseEntity<PerfilVitrine> getMinhaVitrine(@AuthenticationPrincipal Usuario usuario) {
        PerfilVitrine vitrine = vitrineService.buscarPorUsuarioId(usuario.getIdUsuario());
        return ResponseEntity.ok(vitrine);
    }

    // Endpoint para usuários logados verem a vitrine de OUTROS usuários
    @GetMapping("/{usuarioId}")
    public ResponseEntity<PerfilVitrine> getVitrinePorUsuario(@PathVariable Long usuarioId) {
        PerfilVitrine vitrine = vitrineService.buscarPorUsuarioId(usuarioId);
        return ResponseEntity.ok(vitrine);
    }

    @PostMapping("/upload")
    public ResponseEntity<PerfilVitrine> uploadMidia(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("tipo") String tipo,
            @AuthenticationPrincipal Usuario usuario
    ) {
        // Validações e falhas do Cloudinary viram erros com mensagem (400/413/502) pelo GlobalExceptionHandler.
        PerfilVitrine vitrineAtualizada = vitrineService.adicionarMidia(usuario.getIdUsuario(), arquivo, tipo);
        return ResponseEntity.ok(vitrineAtualizada);
    }
}
