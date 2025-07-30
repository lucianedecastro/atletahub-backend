package br.com.atletahub.atletahub_backend.service;

import br.com.atletahub.atletahub_backend.dto.perfil.DadosAtualizacaoPerfilAtleta;
import br.com.atletahub.atletahub_backend.model.TipoUsuario;
import br.com.atletahub.atletahub_backend.model.PerfilAtleta;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.repository.PerfilAtletaRepository;
import br.com.atletahub.atletahub_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PerfilAtletaService {

    @Autowired
    private PerfilAtletaRepository perfilAtletaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;


    @Transactional
    public void criarPerfilAtletaInicial(Usuario usuario) {
        Optional<PerfilAtleta> perfilExistente = perfilAtletaRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());

        if (usuario.getTipoUsuario() == TipoUsuario.ATLETA && perfilExistente.isEmpty()) {
            PerfilAtleta novoPerfil = new PerfilAtleta(usuario);
            perfilAtletaRepository.save(novoPerfil);
        }
    }


    @Transactional
    public PerfilAtleta atualizarPerfilAtleta(Long idUsuario, DadosAtualizacaoPerfilAtleta dados) {

        PerfilAtleta perfil = this.buscarPerfilAtletaPorIdUsuario(idUsuario);
        perfil.atualizar(dados);
        return perfilAtletaRepository.save(perfil);
    }

    @Transactional
    public PerfilAtleta buscarPerfilAtletaPorIdUsuario(Long idUsuario) {
        Optional<PerfilAtleta> perfilOptional = perfilAtletaRepository.findByUsuario_IdUsuario(idUsuario);

        if (perfilOptional.isPresent()) {
            return perfilOptional.get();
        } else {
            Usuario usuario = usuarioRepository.findById(idUsuario)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado ao tentar criar perfil de atleta para o ID: " + idUsuario));

            PerfilAtleta novoPerfil = new PerfilAtleta(usuario);
            return perfilAtletaRepository.save(novoPerfil);
        }
    }
}