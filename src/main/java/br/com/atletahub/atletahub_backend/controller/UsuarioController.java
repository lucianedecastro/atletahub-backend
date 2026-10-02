package br.com.atletahub.atletahub_backend.controller;

import br.com.atletahub.atletahub_backend.dto.usuario.DadosDetalhamentoUsuario;
import br.com.atletahub.atletahub_backend.model.PerfilAtleta;
import br.com.atletahub.atletahub_backend.model.PerfilMarca;
import br.com.atletahub.atletahub_backend.model.TipoUsuario;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.repository.PerfilAtletaRepository;
import br.com.atletahub.atletahub_backend.repository.PerfilMarcaRepository;
import br.com.atletahub.atletahub_backend.service.BloqueioService;
import br.com.atletahub.atletahub_backend.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // Os perfis ficam em tabelas separadas (entidades desacopladas); são buscados em lote abaixo.
    @Autowired
    private PerfilAtletaRepository perfilAtletaRepository;

    @Autowired
    private PerfilMarcaRepository perfilMarcaRepository;

    @Autowired
    private BloqueioService bloqueioService;

    // Lista completa: só ADMIN (regra no SecurityConfig). Já com e-mail, pois é visão administrativa.
    @GetMapping
    public ResponseEntity<List<DadosDetalhamentoUsuario>> listarTodos(@AuthenticationPrincipal Usuario logado) {
        return ResponseEntity.ok(converterLista(usuarioService.listarTodos(), logado));
    }

    @GetMapping("/tipo")
    public ResponseEntity<List<DadosDetalhamentoUsuario>> listarPorTipo(
            @RequestParam("tipoUsuario") String tipoUsuario,
            @AuthenticationPrincipal Usuario logado) {

        List<Usuario> usuarios = usuarioService.buscarPorTipo(tipoUsuario, logado);
        return ResponseEntity.ok(converterLista(usuarios, logado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoUsuario> buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario logado) {

        // Quem foi bloqueado não vê o perfil de quem bloqueou (resposta igual à de perfil inexistente).
        if (logado != null && logado.getTipoUsuario() != TipoUsuario.ADMIN
                && bloqueioService.fuiBloqueadoPor(id, logado.getIdUsuario())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
        }

        Usuario usuario = usuarioService.buscarPorId(id);
        return ResponseEntity.ok(converterLista(List.of(usuario), logado).get(0));
    }

    /**
     * Monta os DTOs com 2 consultas no total (todos os perfis de atleta + todos os de marca),
     * em vez de 2 consultas POR usuário. O e-mail só aparece para o próprio usuário ou para ADMIN.
     */
    private List<DadosDetalhamentoUsuario> converterLista(List<Usuario> usuarios, Usuario logado) {
        if (usuarios.isEmpty()) {
            return List.of();
        }

        List<Long> ids = usuarios.stream().map(Usuario::getIdUsuario).collect(Collectors.toList());

        Map<Long, PerfilAtleta> atletas = new HashMap<>();
        for (PerfilAtleta p : perfilAtletaRepository.findByUsuarioIdIn(ids)) {
            atletas.put(p.getUsuarioId(), p);
        }
        Map<Long, PerfilMarca> marcas = new HashMap<>();
        for (PerfilMarca p : perfilMarcaRepository.findByUsuarioIdIn(ids)) {
            marcas.put(p.getUsuarioId(), p);
        }

        boolean logadoEhAdmin = logado != null && logado.getTipoUsuario() == TipoUsuario.ADMIN;

        return usuarios.stream()
                .map(u -> {
                    boolean mostrarEmail = logadoEhAdmin
                            || (logado != null && logado.getIdUsuario().equals(u.getIdUsuario()));
                    return new DadosDetalhamentoUsuario(
                            u, atletas.get(u.getIdUsuario()), marcas.get(u.getIdUsuario()), mostrarEmail);
                })
                .collect(Collectors.toList());
    }
}
