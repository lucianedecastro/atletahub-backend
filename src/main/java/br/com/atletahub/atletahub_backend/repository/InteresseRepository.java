package br.com.atletahub.atletahub_backend.repository;

import br.com.atletahub.atletahub_backend.model.Interesse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InteresseRepository extends JpaRepository<Interesse, Long> {


    Optional<Interesse> findByOrigem_IdUsuarioAndDestino_IdUsuario(Long origemId, Long destinoId);


    List<Interesse> findByOrigem_IdUsuario(Long origemId);


    List<Interesse> findByDestino_IdUsuario(Long destinoId);
}
