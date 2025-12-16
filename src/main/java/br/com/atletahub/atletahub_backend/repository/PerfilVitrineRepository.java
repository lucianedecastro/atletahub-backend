package br.com.atletahub.atletahub_backend.repository;

import br.com.atletahub.atletahub_backend.model.mongo.PerfilVitrine;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PerfilVitrineRepository extends MongoRepository<PerfilVitrine, String> {

    // Busca a vitrine pelo ID do usuário (aquele Long que vem do PostgreSQL)
    Optional<PerfilVitrine> findByUsuarioId(Long usuarioId);

}