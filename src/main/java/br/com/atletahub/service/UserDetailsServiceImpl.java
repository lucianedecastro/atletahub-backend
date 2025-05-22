package br.com.atletahub.service;

import br.com.atletahub.model.Usuario;
import br.com.atletahub.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Carrega o usuário pelo nome de usuário (neste caso, o email).
     * Este método é chamado pelo Spring Security durante o processo de autenticação.
     *
     * @param email O email do usuário que está tentando se autenticar.
     * @return Um objeto UserDetails contendo informações do usuário e suas autoridades.
     * @throws UsernameNotFoundException Se o usuário com o email fornecido não for encontrado.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Busca o usuário no banco de dados pelo email
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o email: " + email));

        // Converte o objeto Usuario para um objeto UserDetails do Spring Security
        // Aqui, usamos o construtor de User que espera: username, password e collection de authorities
        // Por enquanto, as autoridades serão uma lista vazia, pois ainda não definimos roles/permissoes.
        return new User(usuario.getEmail(), usuario.getSenhaHash(), new ArrayList<>());
    }
}