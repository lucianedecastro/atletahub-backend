package br.com.atletahub.atletahub_backend.service;

import br.com.atletahub.atletahub_backend.dto.perfil.DadosAtualizacaoPerfilAtleta;
import br.com.atletahub.atletahub_backend.model.TipoUsuario;
import br.com.atletahub.atletahub_backend.model.PerfilAtleta;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.repository.PerfilAtletaRepository;
import br.com.atletahub.atletahub_backend.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PerfilAtletaService {

    @Autowired
    private PerfilAtletaRepository perfilAtletaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Método para criar um perfil de atleta inicial quando um usuário do tipo ATLETA é registrado
    @Transactional
    public void criarPerfilAtletaInicial(Usuario usuario) {
        // Verifica se o usuário é do tipo ATLETA
        // E se ainda não tem um perfil associado a esse usuário


        Optional<PerfilAtleta> perfilExistente = perfilAtletaRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());

        if (usuario.getTipoUsuario() == TipoUsuario.ATLETA && perfilExistente.isEmpty()) {
            PerfilAtleta novoPerfil = new PerfilAtleta(usuario);
            perfilAtletaRepository.save(novoPerfil);
        }
    }

    // Método para atualizar um perfil de atleta
    @Transactional
    public PerfilAtleta atualizarPerfilAtleta(Long idUsuario, DadosAtualizacaoPerfilAtleta dados) {
        // 1. Encontrar o PerfilAtleta pelo ID do Usuário

        PerfilAtleta perfil = perfilAtletaRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Perfil de atleta não encontrado para o usuário com ID: " + idUsuario));

        // 2. Chamar o método de atualização na entidade PerfilAtleta
        perfil.atualizar(dados);

        // 3. Salvar as alterações (o @Transactional já garante a persistência)
        return perfilAtletaRepository.save(perfil);
    }

    // Método para buscar um perfil de atleta pelo ID do usuário
    public PerfilAtleta buscarPerfilAtletaPorIdUsuario(Long idUsuario) {

        return perfilAtletaRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Perfil de atleta não encontrado para o usuário com ID: " + idUsuario));
    }
}