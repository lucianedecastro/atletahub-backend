package br.com.atletahub.atletahub_backend.repository;

import br.com.atletahub.atletahub_backend.model.PerfilMarca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PerfilMarcaRepository extends JpaRepository<PerfilMarca, Long> {
    Optional<PerfilMarca> findByUsuarioId(Long usuarioId);

    // Busca em lote (evita 1 consulta por usuário nas listagens)
    List<PerfilMarca> findByUsuarioIdIn(Collection<Long> usuarioIds);
}
