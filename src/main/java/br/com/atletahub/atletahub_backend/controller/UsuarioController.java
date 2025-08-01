package br.com.atletahub.atletahub_backend.controller;

import br.com.atletahub.atletahub_backend.dto.usuario.DadosDetalhamentoUsuario;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;


    @GetMapping
    public ResponseEntity<List<DadosDetalhamentoUsuario>> listarTodos() {
        List<Usuario> usuarios = usuarioService.listarTodos();
        List<DadosDetalhamentoUsuario> detalhes = usuarios.stream()
                .map(DadosDetalhamentoUsuario::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(detalhes);
    }


    @GetMapping("/tipo")
    @PreAuthorize("hasAnyRole('ATLETA', 'MARCA', 'ADMIN')")
    public ResponseEntity<List<DadosDetalhamentoUsuario>> listarPorTipo(
            @RequestParam("tipoUsuario") String tipoUsuario, Authentication authentication) {

        List<Usuario> usuarios = usuarioService.buscarPorTipo(tipoUsuario);
        List<DadosDetalhamentoUsuario> detalhes = usuarios.stream()
                .map(DadosDetalhamentoUsuario::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(detalhes);
    }


    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoUsuario> buscarPorId(@PathVariable Long id) {
        Usuario usuario = usuarioService.buscarPorId(id);
        DadosDetalhamentoUsuario detalhes = new DadosDetalhamentoUsuario(usuario);
        return ResponseEntity.ok(detalhes);
    }
}