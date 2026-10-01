package br.com.atletahub.atletahub_backend.repository;

import br.com.atletahub.atletahub_backend.model.PerfilAtleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PerfilAtletaRepository extends JpaRepository<PerfilAtleta, Long> {
    Optional<PerfilAtleta> findByUsuarioId(Long usuarioId);

    // Busca em lote (evita 1 consulta por usuário nas listagens)
    List<PerfilAtleta> findByUsuarioIdIn(Collection<Long> usuarioIds);
}
