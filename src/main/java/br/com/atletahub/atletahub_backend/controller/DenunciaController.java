package br.com.atletahub.atletahub_backend.controller;

import br.com.atletahub.atletahub_backend.dto.denuncia.DadosCriacaoDenuncia;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.service.DenunciaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/denuncias")
public class DenunciaController {

    @Autowired
    private DenunciaService denunciaService;

    @PostMapping
    public ResponseEntity<Map<String, String>> denunciar(
            @RequestBody @Valid DadosCriacaoDenuncia dados,
            @AuthenticationPrincipal Usuario usuario) {
        denunciaService.criar(usuario, dados);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Denúncia enviada. Vamos analisar."));
    }
}
