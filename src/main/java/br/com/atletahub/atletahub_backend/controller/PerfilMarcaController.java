package br.com.atletahub.atletahub_backend.controller;

import br.com.atletahub.atletahub_backend.dto.perfil.DadosAtualizacaoPerfilMarca;
import br.com.atletahub.atletahub_backend.dto.perfil.DadosDetalhamentoPerfilMarca;
import br.com.atletahub.atletahub_backend.model.PerfilMarca;
import br.com.atletahub.atletahub_backend.model.Usuario; // Importar a entidade Usuario
import br.com.atletahub.atletahub_backend.service.PerfilMarcaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Importar AuthenticationPrincipal
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/perfil/marca") // Endpoint para perfis de marca
public class PerfilMarcaController {

    @Autowired
    private PerfilMarcaService perfilMarcaService;

    // Endpoint para atualizar o PRÓPRIO perfil da marca

    @PutMapping // Mapeamento apenas para /perfil/marca (PUT)
    @Transactional
    public ResponseEntity<DadosDetalhamentoPerfilMarca> atualizarPerfilMarca(
            @RequestBody @Valid DadosAtualizacaoPerfilMarca dados,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {
        Long idUsuario = usuarioAutenticado.getIdUsuario();

        PerfilMarca perfilAtualizado = perfilMarcaService.atualizarPerfilMarca(idUsuario, dados);

        // Retorna o DTO de detalhamento como resposta
        return ResponseEntity.ok(new DadosDetalhamentoPerfilMarca(perfilAtualizado));
    }

    // Endpoint para buscar o PRÓPRIO perfil da marca
    @GetMapping // Mapeamento apenas para /perfil/marca (GET)
    public ResponseEntity<DadosDetalhamentoPerfilMarca> buscarPerfilMarca(
            @AuthenticationPrincipal Usuario usuarioAutenticado) {
        Long idUsuario = usuarioAutenticado.getIdUsuario();
        PerfilMarca perfil = perfilMarcaService.buscarPerfilMarcaPorIdUsuario(idUsuario);
        return ResponseEntity.ok(new DadosDetalhamentoPerfilMarca(perfil));
    }


}