package br.com.atletahub.atletahub_backend.repository;

import br.com.atletahub.atletahub_backend.model.Mensagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensagemRepository extends JpaRepository<Mensagem, Long> {

    List<Mensagem> findByMatchIdOrderByDataEnvioAsc(Long idMatch);


}
