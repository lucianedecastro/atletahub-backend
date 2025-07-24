package br.com.atletahub.atletahub_backend.repository;

import br.com.atletahub.atletahub_backend.model.Interesse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InteresseRepository extends JpaRepository<Interesse, Long> {

    // Método para buscar um interesse específico entre origem e destino
    // Isso é útil para verificar se um interesse já existe (ex: A já curtiu B?)
    Optional<Interesse> findByOrigem_IdUsuarioAndDestino_IdUsuario(Long origemId, Long destinoId);

    // Método para listar todos os interesses enviados por um usuário
    List<Interesse> findByOrigem_IdUsuario(Long origemId);

    // Método para listar todos os interesses recebidos por um usuário
    List<Interesse> findByDestino_IdUsuario(Long destinoId);
}
