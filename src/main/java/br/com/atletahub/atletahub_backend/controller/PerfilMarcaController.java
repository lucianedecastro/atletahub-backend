package br.com.atletahub.atletahub_backend.controller;

import br.com.atletahub.atletahub_backend.dto.perfil.DadosAtualizacaoPerfilMarca;
import br.com.atletahub.atletahub_backend.dto.perfil.DadosDetalhamentoPerfilMarca;
import br.com.atletahub.atletahub_backend.model.PerfilMarca;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.service.PerfilMarcaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/perfil/marca")
public class PerfilMarcaController {

    @Autowired
    private PerfilMarcaService perfilMarcaService;



    @PutMapping
    @Transactional
    public ResponseEntity<DadosDetalhamentoPerfilMarca> atualizarPerfilMarca(
            @RequestBody @Valid DadosAtualizacaoPerfilMarca dados,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {
        Long idUsuario = usuarioAutenticado.getIdUsuario();

        PerfilMarca perfilAtualizado = perfilMarcaService.atualizarPerfilMarca(idUsuario, dados);


        return ResponseEntity.ok(new DadosDetalhamentoPerfilMarca(perfilAtualizado));
    }


    @GetMapping
    public ResponseEntity<DadosDetalhamentoPerfilMarca> buscarPerfilMarca(
            @AuthenticationPrincipal Usuario usuarioAutenticado) {
        Long idUsuario = usuarioAutenticado.getIdUsuario();
        PerfilMarca perfil = perfilMarcaService.buscarPerfilMarcaPorIdUsuario(idUsuario);
        return ResponseEntity.ok(new DadosDetalhamentoPerfilMarca(perfil));
    }


}