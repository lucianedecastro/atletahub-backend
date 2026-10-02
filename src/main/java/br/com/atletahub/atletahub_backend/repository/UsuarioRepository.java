package br.com.atletahub.atletahub_backend.repository;

import br.com.atletahub.atletahub_backend.model.StatusConta;
import br.com.atletahub.atletahub_backend.model.TipoUsuario;
import br.com.atletahub.atletahub_backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByTipoUsuario(TipoUsuario tipo);

    // Descobrir: só contas ativas aparecem para os outros usuários.
    List<Usuario> findByTipoUsuarioAndStatus(TipoUsuario tipo, StatusConta status);

    // Usados para impedir e-mail duplicado (ignorando maiúsculas/minúsculas) no cadastro e na edição de perfil.
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdUsuarioNot(String email, Long idUsuario);

    // Números do painel admin.
    long countByTipoUsuarioAndStatus(TipoUsuario tipo, StatusConta status);

    // Contas novas gravam o aceite dos termos no cadastro: serve como data de criação.
    long countByTermosAceitosEmAfter(Instant desde);
}
