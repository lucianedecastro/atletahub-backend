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

    // Endpoint para buscar todos os usuários (geral, pode ser usado para testes)
    @GetMapping
    public ResponseEntity<List<DadosDetalhamentoUsuario>> listarTodos() {
        List<Usuario> usuarios = usuarioService.listarTodos();
        List<DadosDetalhamentoUsuario> detalhes = usuarios.stream()
                .map(DadosDetalhamentoUsuario::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(detalhes);
    }

    // Endpoint para buscar todos os usuários do tipo ATLETA ou MARCA

    @GetMapping("/tipo")
    @PreAuthorize("hasAnyRole('ATLETA', 'MARCA', 'ADMIN')")
    public ResponseEntity<List<DadosDetalhamentoUsuario>> listarPorTipo(
            @RequestParam("tipoUsuario") String tipoUsuario, Authentication authentication) {

        // Regra de negócio: Apenas usuários autenticados podem filtrar por tipo
        List<Usuario> usuarios = usuarioService.buscarPorTipo(tipoUsuario);
        List<DadosDetalhamentoUsuario> detalhes = usuarios.stream()
                .map(DadosDetalhamentoUsuario::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(detalhes);
    }

    // Endpoint para buscar um único usuário por ID (usado na página de perfil)
    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoUsuario> buscarPorId(@PathVariable Long id) {
        Usuario usuario = usuarioService.buscarPorId(id);
        DadosDetalhamentoUsuario detalhes = new DadosDetalhamentoUsuario(usuario);
        return ResponseEntity.ok(detalhes);
    }
}
