package br.com.atletahub.atletahub_backend.dto.match;

import br.com.atletahub.atletahub_backend.enums.TipoMatch;
import br.com.atletahub.atletahub_backend.model.Match;

import java.time.LocalDateTime;
import java.util.Map;

public record DadosDetalhamentoMatch(
        Long id,
        Long idUsuarioA,
        Long idUsuarioB,
        String nomeUsuarioA,
        String nomeUsuarioB,
        String nomeOutroUsuario,
        // Foto/logo do outro participante (null quando ainda não enviou)
        String fotoOutroUsuario,
        TipoMatch tipoMatch,
        LocalDateTime dataMatch
) {

    /** fotos: id do usuário -> url da imagem de perfil (pode estar vazio ou não ter o usuário). */
    public DadosDetalhamentoMatch(Match match, Long idUsuarioLogado, Map<Long, String> fotos) {
        this(
                match.getId(),
                match.getUsuarioA().getIdUsuario(),
                match.getUsuarioB().getIdUsuario(),
                match.getUsuarioA().getNome(),
                match.getUsuarioB().getNome(),
                match.getUsuarioA().getIdUsuario().equals(idUsuarioLogado) ? match.getUsuarioB().getNome() : match.getUsuarioA().getNome(),
                fotos.get(match.getUsuarioA().getIdUsuario().equals(idUsuarioLogado)
                        ? match.getUsuarioB().getIdUsuario()
                        : match.getUsuarioA().getIdUsuario()),
                match.getTipoMatch(),
                match.getDataMatch()
        );
    }

    public DadosDetalhamentoMatch(Match match, Long idUsuarioLogado) {
        this(match, idUsuarioLogado, Map.of());
    }
}
