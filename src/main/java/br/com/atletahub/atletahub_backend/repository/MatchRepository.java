package br.com.atletahub.atletahub_backend.repository;

import br.com.atletahub.atletahub_backend.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    // Para verificar se um match específico já existe entre dois usuários

    Optional<Match> findByUsuarioA_IdUsuarioAndUsuarioB_IdUsuario(Long usuarioAId, Long usuarioBId);

    // Para listar todos os matches de um determinado usuário, seja ele usuarioA ou usuarioB
    List<Match> findByUsuarioA_IdUsuarioOrUsuarioB_IdUsuario(Long usuarioAId, Long usuarioBId);


}