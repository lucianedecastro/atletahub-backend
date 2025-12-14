package br.com.atletahub.atletahub_backend.controller;

import br.com.atletahub.atletahub_backend.dto.usuario.DadosDetalhamentoUsuario;
import br.com.atletahub.atletahub_backend.model.PerfilAtleta;
import br.com.atletahub.atletahub_backend.model.PerfilMarca;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.repository.PerfilAtletaRepository;
import br.com.atletahub.atletahub_backend.repository.PerfilMarcaRepository;
import br.com.atletahub.atletahub_backend.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // Precisamos dos repositórios para buscar os perfis manualmente
    // já que desacoplamos as entidades para evitar o erro do Hibernate
    @Autowired
    private PerfilAtletaRepository perfilAtletaRepository;

    @Autowired
    private PerfilMarcaRepository perfilMarcaRepository;


    @GetMapping
    public ResponseEntity<List<DadosDetalhamentoUsuario>> listarTodos() {
        List<Usuario> usuarios = usuarioService.listarTodos();

        List<DadosDetalhamentoUsuario> detalhes = usuarios.stream()
                .map(this::converterParaDto) // Usa método auxiliar para montar o DTO
                .collect(Collectors.toList());

        return ResponseEntity.ok(detalhes);
    }


    @GetMapping("/tipo")
    @PreAuthorize("hasAnyRole('ATLETA', 'MARCA', 'ADMIN')")
    public ResponseEntity<List<DadosDetalhamentoUsuario>> listarPorTipo(
            @RequestParam("tipoUsuario") String tipoUsuario, Authentication authentication) {

        List<Usuario> usuarios = usuarioService.buscarPorTipo(tipoUsuario);

        List<DadosDetalhamentoUsuario> detalhes = usuarios.stream()
                .map(this::converterParaDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(detalhes);
    }


    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoUsuario> buscarPorId(@PathVariable Long id) {
        Usuario usuario = usuarioService.buscarPorId(id);

        DadosDetalhamentoUsuario detalhes = converterParaDto(usuario);

        return ResponseEntity.ok(detalhes);
    }

    /**
     * Método auxiliar que busca os dados complementares (Atleta ou Marca)
     * e monta o DTO completo.
     */
    private DadosDetalhamentoUsuario converterParaDto(Usuario usuario) {
        // Busca perfil de atleta pelo ID do usuário (retorna null se não achar)
        PerfilAtleta atleta = perfilAtletaRepository.findByUsuarioId(usuario.getIdUsuario()).orElse(null);

        // Busca perfil de marca pelo ID do usuário (retorna null se não achar)
        PerfilMarca marca = perfilMarcaRepository.findByUsuarioId(usuario.getIdUsuario()).orElse(null);

        // O construtor do DTO agora sabe lidar com nulos e montar o JSON correto
        return new DadosDetalhamentoUsuario(usuario, atleta, marca);
    }
}