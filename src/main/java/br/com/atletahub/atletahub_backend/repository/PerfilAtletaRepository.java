package br.com.atletahub.atletahub_backend.repository;

import br.com.atletahub.atletahub_backend.model.PerfilAtleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PerfilAtletaRepository extends JpaRepository<PerfilAtleta, Long> {
    Optional<PerfilAtleta> findByUsuarioId(Long usuarioId);

}