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
        // Ajuste: Busca pelo ID direto (findByUsuarioId)
        Optional<PerfilAtleta> perfilExistente = perfilAtletaRepository.findByUsuarioId(usuario.getIdUsuario());

        if (usuario.getTipoUsuario() == TipoUsuario.ATLETA && perfilExistente.isEmpty()) {
            // Ajuste: Passa apenas o ID Long para o construtor
            PerfilAtleta novoPerfil = new PerfilAtleta(usuario.getIdUsuario());
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
        // Ajuste: Busca pelo ID direto
        Optional<PerfilAtleta> perfilOptional = perfilAtletaRepository.findByUsuarioId(idUsuario);

        if (perfilOptional.isPresent()) {
            return perfilOptional.get();
        } else {
            Usuario usuario = usuarioRepository.findById(idUsuario)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado ao tentar criar perfil de atleta para o ID: " + idUsuario));

            PerfilAtleta novoPerfil = new PerfilAtleta(usuario.getIdUsuario());
            return perfilAtletaRepository.save(novoPerfil);
        }
    }
}