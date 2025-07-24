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

import java.util.Optional; // Importe Optional para usar isEmpty()

@Service
public class PerfilMarcaService {

    @Autowired
    private PerfilMarcaRepository perfilMarcaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Método para criar um perfil de marca inicial quando um usuário do tipo MARCA é registrado
    @Transactional
    public void criarPerfilMarcaInicial(Usuario usuario) {
        // Verifica se o usuário é do tipo MARCA e se ainda não tem um perfil de marca

        Optional<PerfilMarca> perfilExistente = perfilMarcaRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());

        if (usuario.getTipoUsuario() == TipoUsuario.MARCA && perfilExistente.isEmpty()) {
            PerfilMarca novoPerfil = new PerfilMarca(usuario);
            perfilMarcaRepository.save(novoPerfil);
        }
    }

    // Método para atualizar um perfil de marca
    @Transactional
    public PerfilMarca atualizarPerfilMarca(Long idUsuario, DadosAtualizacaoPerfilMarca dados) {
        // 1. Encontrar o PerfilMarca pelo ID do Usuário

        PerfilMarca perfil = perfilMarcaRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Perfil de marca não encontrado para o usuário: " + idUsuario));

        // 2. Chamar o método de atualização na entidade PerfilMarca

        perfil.atualizar(dados);

        // 3. Salvar as alterações no banco de dados
        return perfilMarcaRepository.save(perfil);
    }

    // Método para buscar um perfil de marca pelo ID do usuário
    public PerfilMarca buscarPerfilMarcaPorIdUsuario(Long idUsuario) {

        return perfilMarcaRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Perfil de marca não encontrado para o usuário: " + idUsuario));
    }
}