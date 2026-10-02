package br.com.atletahub.atletahub_backend.repository;

import br.com.atletahub.atletahub_backend.model.RedefinicaoSenha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RedefinicaoSenhaRepository extends JpaRepository<RedefinicaoSenha, Long> {

    Optional<RedefinicaoSenha> findByTokenHash(String tokenHash);

    // Limite de pedidos por conta (evita encher a caixa de e-mail de alguém).
    long countByIdUsuarioAndCriadaEmAfter(Long idUsuario, Instant desde);

    // Marca como usados todos os links ainda abertos da conta: só o link mais novo funciona.
    @Modifying
    @Query("update RedefinicaoSenha r set r.usadaEm = :agora where r.idUsuario = :idUsuario and r.usadaEm is null")
    int invalidarAbertos(@Param("idUsuario") Long idUsuario, @Param("agora") Instant agora);

    // Limpeza: pedidos vencidos há muito tempo não servem para nada.
    @Modifying
    @Query("delete from RedefinicaoSenha r where r.expiraEm < :limite")
    int apagarVencidosAntesDe(@Param("limite") Instant limite);
}
