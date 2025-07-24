package br.com.atletahub.atletahub_backend.repository;

import br.com.atletahub.atletahub_backend.model.Mensagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository // Marca a interface como um componente de repositório Spring
public interface MensagemRepository extends JpaRepository<Mensagem, Long> {

    // Método para buscar todas as mensagens de um determinado match, ordenadas pela data de envio
    List<Mensagem> findByMatchIdOrderByDataEnvioAsc(Long idMatch);


}
