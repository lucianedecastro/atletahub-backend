package br.com.atletahub.atletahub_backend.controller;

import br.com.atletahub.atletahub_backend.dto.perfil.DadosAtualizacaoPerfilAtleta;
import br.com.atletahub.atletahub_backend.model.PerfilAtleta;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.service.PerfilAtletaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/perfil/atleta")
public class PerfilAtletaController {

    @Autowired
    private PerfilAtletaService perfilAtletaService;

    @GetMapping
    public ResponseEntity<PerfilAtleta> obterPerfilAtleta(@AuthenticationPrincipal Usuario usuarioLogado) {

        PerfilAtleta perfil = perfilAtletaService.buscarPerfilAtletaPorIdUsuario(usuarioLogado.getIdUsuario());
        return ResponseEntity.ok(perfil);
    }

    @PutMapping
    public ResponseEntity<PerfilAtleta> atualizarPerfilAtleta(
            @RequestBody @Valid DadosAtualizacaoPerfilAtleta dados,
            @AuthenticationPrincipal Usuario usuarioLogado) {


        PerfilAtleta perfilAtualizado = perfilAtletaService.atualizarPerfilAtleta(usuarioLogado.getIdUsuario(), dados);
        return ResponseEntity.ok(perfilAtualizado);
    }
}