package br.com.atletahub.atletahub_backend.repository;

import br.com.atletahub.atletahub_backend.model.MensagemTraducao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MensagemTraducaoRepository extends JpaRepository<MensagemTraducao, Long> {

    MensagemTraducao findByMensagem_IdAndIdiomaDestino(
            Long idMensagem,
            String idiomaDestino
    );
}
