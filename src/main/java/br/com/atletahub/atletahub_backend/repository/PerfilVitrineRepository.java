package br.com.atletahub.atletahub_backend.repository;

import br.com.atletahub.atletahub_backend.model.mongo.PerfilVitrine;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface PerfilVitrineRepository extends MongoRepository<PerfilVitrine, String> {
    Optional<PerfilVitrine> findByUsuarioId(Long usuarioId);
}
