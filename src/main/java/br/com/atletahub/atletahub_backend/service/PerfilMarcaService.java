package br.com.atletahub.atletahub_backend.service;

import br.com.atletahub.atletahub_backend.dto.perfil.DadosAtualizacaoPerfilMarca;
import br.com.atletahub.atletahub_backend.model.TipoUsuario;
import br.com.atletahub.atletahub_backend.model.PerfilMarca;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.repository.PerfilMarcaRepository;
import br.com.atletahub.atletahub_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PerfilMarcaService {

    @Autowired
    private PerfilMarcaRepository perfilMarcaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;


    @Transactional
    public void criarPerfilMarcaInicial(Usuario usuario) {
        Optional<PerfilMarca> perfilExistente = perfilMarcaRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());

        if (usuario.getTipoUsuario() == TipoUsuario.MARCA && perfilExistente.isEmpty()) {
            PerfilMarca novoPerfil = new PerfilMarca(usuario);
            perfilMarcaRepository.save(novoPerfil);
        }
    }


    @Transactional
    public PerfilMarca atualizarPerfilMarca(Long idUsuario, DadosAtualizacaoPerfilMarca dados) {
        PerfilMarca perfil = this.buscarPerfilMarcaPorIdUsuario(idUsuario);
        perfil.atualizar(dados);
        return perfilMarcaRepository.save(perfil);
    }


    @Transactional
    public PerfilMarca buscarPerfilMarcaPorIdUsuario(Long idUsuario) {
        Optional<PerfilMarca> perfilOptional = perfilMarcaRepository.findByUsuario_IdUsuario(idUsuario);

        if (perfilOptional.isPresent()) {
            return perfilOptional.get();
        } else {
            Usuario usuario = usuarioRepository.findById(idUsuario)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado ao tentar criar perfil de marca para o ID: " + idUsuario));

            PerfilMarca novoPerfil = new PerfilMarca(usuario);
            return perfilMarcaRepository.save(novoPerfil);
        }
    }
}