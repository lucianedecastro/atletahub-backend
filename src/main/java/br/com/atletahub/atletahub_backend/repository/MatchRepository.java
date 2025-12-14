package br.com.atletahub.atletahub_backend.repository;

import br.com.atletahub.atletahub_backend.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

   Optional<Match> findByUsuarioA_IdUsuarioAndUsuarioB_IdUsuario(Long usuarioAId, Long usuarioBId);


   List<Match> findByUsuarioA_IdUsuarioOrUsuarioB_IdUsuario(Long usuarioAId, Long usuarioBId);


}