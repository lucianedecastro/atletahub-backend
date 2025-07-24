package br.com.atletahub.atletahub_backend.dto.match;

import br.com.atletahub.atletahub_backend.enums.TipoMatch;
import br.com.atletahub.atletahub_backend.model.Match;

import java.time.LocalDateTime;

public record DadosDetalhamentoMatch(
        Long id,
        Long idUsuarioA,
        Long idUsuarioB,
        TipoMatch tipoMatch,
        LocalDateTime dataMatch
) {
    // Construtor que recebe a entidade Match e converte para o DTO
    public DadosDetalhamentoMatch(Match match) {
        this(
                match.getId(),
                match.getUsuarioA().getIdUsuario(), // Pega o ID do objeto Usuario A
                match.getUsuarioB().getIdUsuario(), // Pega o ID do objeto Usuario B
                match.getTipoMatch(),
                match.getDataMatch()
        );
    }
}
