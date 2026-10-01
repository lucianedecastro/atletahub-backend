package br.com.atletahub.atletahub_backend.service;

import br.com.atletahub.atletahub_backend.dto.perfil.DadosAtualizacaoPerfilAtleta;
import br.com.atletahub.atletahub_backend.model.PerfilAtleta;
import br.com.atletahub.atletahub_backend.model.TipoUsuario;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.repository.PerfilAtletaRepository;
import br.com.atletahub.atletahub_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class PerfilAtletaService {

    @Autowired
    private PerfilAtletaRepository perfilAtletaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public void criarPerfilAtletaInicial(Usuario usuario) {
        Optional<PerfilAtleta> perfilExistente = perfilAtletaRepository.findByUsuarioId(usuario.getIdUsuario());
        if (usuario.getTipoUsuario() == TipoUsuario.ATLETA && perfilExistente.isEmpty()) {
            PerfilAtleta novoPerfil = new PerfilAtleta(usuario.getIdUsuario());
            perfilAtletaRepository.save(novoPerfil);
        }
    }

    @Transactional
    public PerfilAtleta atualizarPerfilAtleta(Long idUsuario, DadosAtualizacaoPerfilAtleta dados) {
        // 1. Atualiza Perfil
        PerfilAtleta perfil = this.buscarPerfilAtletaPorIdUsuario(idUsuario);
        perfil.atualizar(dados);

        // 2. Atualiza Usuário (Nome/Email)
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        boolean mudouUsuario = false;
        if (dados.nome() != null && !dados.nome().isBlank()) {
            usuario.setNome(dados.nome().trim());
            mudouUsuario = true;
        }
        if (dados.email() != null && !dados.email().isBlank()
                && !dados.email().equals(usuario.getEmail())) {
            // Antes qualquer e-mail era aceito: se já existisse em outra conta, estourava erro 500 no banco.
            if (usuarioRepository.existsByEmailIgnoreCaseAndIdUsuarioNot(dados.email(), idUsuario)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está em uso.");
            }
            usuario.setEmail(dados.email());
            mudouUsuario = true;
        }

        if (mudouUsuario) {
            usuarioRepository.save(usuario);
        }

        return perfilAtletaRepository.save(perfil);
    }

    @Transactional
    public PerfilAtleta buscarPerfilAtletaPorIdUsuario(Long idUsuario) {
        Optional<PerfilAtleta> perfilOptional = perfilAtletaRepository.findByUsuarioId(idUsuario);
        if (perfilOptional.isPresent()) {
            return perfilOptional.get();
        } else {
            Usuario usuario = usuarioRepository.findById(idUsuario)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
            PerfilAtleta novoPerfil = new PerfilAtleta(usuario.getIdUsuario());
            return perfilAtletaRepository.save(novoPerfil);
        }
    }
}
