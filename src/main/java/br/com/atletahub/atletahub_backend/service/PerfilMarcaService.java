package br.com.atletahub.atletahub_backend.service;

import br.com.atletahub.atletahub_backend.dto.perfil.DadosAtualizacaoPerfilMarca;
import br.com.atletahub.atletahub_backend.model.TipoUsuario;
import br.com.atletahub.atletahub_backend.model.PerfilMarca;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.repository.PerfilMarcaRepository;
import br.com.atletahub.atletahub_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class PerfilMarcaService {

    @Autowired
    private PerfilMarcaRepository perfilMarcaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public void criarPerfilMarcaInicial(Usuario usuario) {
        Optional<PerfilMarca> perfilExistente = perfilMarcaRepository.findByUsuarioId(usuario.getIdUsuario());
        if (usuario.getTipoUsuario() == TipoUsuario.MARCA && perfilExistente.isEmpty()) {
            PerfilMarca novoPerfil = new PerfilMarca(usuario.getIdUsuario());
            perfilMarcaRepository.save(novoPerfil);
        }
    }

    @Transactional
    public PerfilMarca atualizarPerfilMarca(Long idUsuario, DadosAtualizacaoPerfilMarca dados) {
        // 1. Atualiza dados do Perfil (Tabela perfil_marca)
        PerfilMarca perfil = this.buscarPerfilMarcaPorIdUsuario(idUsuario);
        perfil.atualizar(dados); // Atualiza produto, logo, etc.

        // 2. Atualiza dados do Usuário (Tabela usuario - Nome e Email)
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        boolean mudouUsuario = false;
        if (dados.nome() != null && !dados.nome().isBlank()) {
            usuario.setNome(dados.nome().trim());
            mudouUsuario = true;
        }
        if (dados.email() != null && !dados.email().isBlank()
                && !dados.email().equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmailIgnoreCaseAndIdUsuarioNot(dados.email(), idUsuario)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está em uso.");
            }
            usuario.setEmail(dados.email());
            mudouUsuario = true;
        }

        if (mudouUsuario) {
            usuarioRepository.save(usuario);
        }

        return perfilMarcaRepository.save(perfil);
    }

    @Transactional
    public PerfilMarca buscarPerfilMarcaPorIdUsuario(Long idUsuario) {
        Optional<PerfilMarca> perfilOptional = perfilMarcaRepository.findByUsuarioId(idUsuario);
        if (perfilOptional.isPresent()) {
            return perfilOptional.get();
        } else {
            Usuario usuario = usuarioRepository.findById(idUsuario)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
            PerfilMarca novoPerfil = new PerfilMarca(usuario.getIdUsuario());
            return perfilMarcaRepository.save(novoPerfil);
        }
    }
}
