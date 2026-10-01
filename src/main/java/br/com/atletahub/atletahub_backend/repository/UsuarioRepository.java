package br.com.atletahub.atletahub_backend.repository;

import br.com.atletahub.atletahub_backend.model.TipoUsuario;
import br.com.atletahub.atletahub_backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByTipoUsuario(TipoUsuario tipo);

    // Usados para impedir e-mail duplicado (ignorando maiúsculas/minúsculas) no cadastro e na edição de perfil.
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdUsuarioNot(String email, Long idUsuario);
}
