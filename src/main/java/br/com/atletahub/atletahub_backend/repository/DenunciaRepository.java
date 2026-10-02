package br.com.atletahub.atletahub_backend.repository;

import br.com.atletahub.atletahub_backend.enums.StatusDenuncia;
import br.com.atletahub.atletahub_backend.enums.TipoAlvoDenuncia;
import br.com.atletahub.atletahub_backend.model.Denuncia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface DenunciaRepository extends JpaRepository<Denuncia, Long> {

    Page<Denuncia> findByStatus(StatusDenuncia status, Pageable pageable);

    long countByStatus(StatusDenuncia status);

    List<Denuncia> findByIdDenunciadoOrderByCriadaEmDesc(Long idDenunciado);

    // Freio contra denúncia em massa: quantas o usuário já enviou desde um instante.
    long countByIdDenuncianteAndCriadaEmAfter(Long idDenunciante, Instant desde);

    // Evita a mesma pessoa denunciar o mesmo alvo várias vezes enquanto a primeira está aberta.
    boolean existsByIdDenuncianteAndIdDenunciadoAndTipoAlvoAndStatus(
            Long idDenunciante, Long idDenunciado, TipoAlvoDenuncia tipoAlvo, StatusDenuncia status);
}
