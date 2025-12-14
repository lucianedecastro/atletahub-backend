package br.com.atletahub.atletahub_backend.repository;

import br.com.atletahub.atletahub_backend.model.PerfilMarca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PerfilMarcaRepository extends JpaRepository<PerfilMarca, Long> {
    Optional<PerfilMarca> findByUsuarioId(Long usuarioId);

}