package br.com.atletahub.atletahub_backend.controller;

import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.service.FotoPerfilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/** Foto de perfil (atleta) e logo (marca) do usuário logado. */
@RestController
@RequestMapping("/perfil/foto")
public class FotoPerfilController {

    @Autowired
    private FotoPerfilService fotoPerfilService;

    @PostMapping
    public ResponseEntity<Map<String, String>> enviar(
            @RequestParam("arquivo") MultipartFile arquivo,
            @AuthenticationPrincipal Usuario usuario) {
        String url = fotoPerfilService.atualizar(usuario, arquivo);
        return ResponseEntity.ok(Map.of("fotoUrl", url));
    }

    @DeleteMapping
    public ResponseEntity<Void> remover(@AuthenticationPrincipal Usuario usuario) {
        fotoPerfilService.remover(usuario);
        return ResponseEntity.noContent().build();
    }
}
