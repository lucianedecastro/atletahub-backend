package br.com.atletahub.atletahub_backend.repository;

import br.com.atletahub.atletahub_backend.model.Bloqueio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BloqueioRepository extends JpaRepository<Bloqueio, Long> {

    Optional<Bloqueio> findByIdBloqueadorAndIdBloqueado(Long idBloqueador, Long idBloqueado);

    boolean existsByIdBloqueadorAndIdBloqueado(Long idBloqueador, Long idBloqueado);

    List<Bloqueio> findByIdBloqueadorOrderByCriadoEmDesc(Long idBloqueador);

    /** Existe bloqueio entre as duas pessoas, em qualquer direção. */
    @Query("select count(b) > 0 from Bloqueio b "
            + "where (b.idBloqueador = :a and b.idBloqueado = :b) or (b.idBloqueador = :b and b.idBloqueado = :a)")
    boolean existeEntre(@Param("a") Long a, @Param("b") Long b);

    @Query("select b.idBloqueado from Bloqueio b where b.idBloqueador = :id")
    List<Long> idsQueEuBloqueei(@Param("id") Long id);

    @Query("select b.idBloqueador from Bloqueio b where b.idBloqueado = :id")
    List<Long> idsQueMeBloquearam(@Param("id") Long id);
}
