package br.com.atletahub.atletahub_backend.dto.match;

import br.com.atletahub.atletahub_backend.enums.TipoMatch;
import br.com.atletahub.atletahub_backend.model.Match;
import br.com.atletahub.atletahub_backend.model.Usuario;

import java.time.LocalDateTime;

public record DadosDetalhamentoMatch(
        Long id,
        Long idUsuarioA,
        Long idUsuarioB,
        String nomeUsuarioA,
        String nomeUsuarioB,
        String nomeOutroUsuario,
        TipoMatch tipoMatch,
        LocalDateTime dataMatch
) {

    public DadosDetalhamentoMatch(Match match, Long idUsuarioLogado) {
        this(
                match.getId(),
                match.getUsuarioA().getIdUsuario(),
                match.getUsuarioB().getIdUsuario(),
                match.getUsuarioA().getNome(),
                match.getUsuarioB().getNome(),
                match.getUsuarioA().getIdUsuario().equals(idUsuarioLogado) ? match.getUsuarioB().getNome() : match.getUsuarioA().getNome(),
                match.getTipoMatch(),
                match.getDataMatch()
        );
    }
}