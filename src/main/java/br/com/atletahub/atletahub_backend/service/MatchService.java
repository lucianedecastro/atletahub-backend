package br.com.atletahub.atletahub_backend.service;

import br.com.atletahub.atletahub_backend.dto.match.DadosDetalhamentoMatch;
import br.com.atletahub.atletahub_backend.enums.TipoInteresse;
import br.com.atletahub.atletahub_backend.enums.TipoMatch;
import br.com.atletahub.atletahub_backend.model.Interesse;
import br.com.atletahub.atletahub_backend.model.Match;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.repository.InteresseRepository;
import br.com.atletahub.atletahub_backend.repository.MatchRepository;
import br.com.atletahub.atletahub_backend.repository.PerfilAtletaRepository;
import br.com.atletahub.atletahub_backend.repository.PerfilMarcaRepository;
import br.com.atletahub.atletahub_backend.model.PerfilAtleta;
import br.com.atletahub.atletahub_backend.model.PerfilMarca;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MatchService {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private InteresseRepository interesseRepository;

    @Autowired
    private PerfilAtletaRepository perfilAtletaRepository;

    @Autowired
    private PerfilMarcaRepository perfilMarcaRepository;

    /**
     * Gera o match quando cabe:
     *  - SUPER_CURTIR: match imediato (SUPER_MATCH);
     *  - CURTIR: match (RECIPROCO) só se o outro lado também deu CURTIR.
     * Retorna null quando o match já existia ou quando ainda não há match.
     */
    @Transactional
    public Match verificarEGerarMatch(Usuario origem, Usuario destino, TipoInteresse tipoInteresse) {

        Long idUsuarioA = Math.min(origem.getIdUsuario(), destino.getIdUsuario());
        Long idUsuarioB = Math.max(origem.getIdUsuario(), destino.getIdUsuario());

        Optional<Match> matchExistente = matchRepository.findByUsuarioA_IdUsuarioAndUsuarioB_IdUsuario(idUsuarioA, idUsuarioB);
        if (matchExistente.isPresent()) {
            return null;
        }

        if (tipoInteresse == TipoInteresse.SUPER_CURTIR) {
            return matchRepository.save(new Match(origem, destino, TipoMatch.SUPER_MATCH));
        }

        if (tipoInteresse == TipoInteresse.CURTIR) {
            Optional<Interesse> interesseReciproco = interesseRepository
                    .findByOrigem_IdUsuarioAndDestino_IdUsuario(destino.getIdUsuario(), origem.getIdUsuario());

            if (interesseReciproco.isPresent() && interesseReciproco.get().getTipoInteresse() == TipoInteresse.CURTIR) {
                return matchRepository.save(new Match(origem, destino, TipoMatch.RECIPROCO));
            }
        }
        return null;
    }

    @Transactional(readOnly = true)
    public Match buscarMatchPorId(Long idMatch) {
        return matchRepository.findById(idMatch)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match não encontrado."));
    }

    // Transação de leitura: o DTO acessa usuários do match (carregamento lazy) e isso precisa
    // acontecer com a sessão aberta. É o pré-requisito para desligar o open-in-view no futuro.
    @Transactional(readOnly = true)
    public List<DadosDetalhamentoMatch> listarMatchesDoUsuario(Long idUsuarioLogado) {
        List<Match> matches = matchRepository.findByUsuarioA_IdUsuarioOrUsuarioB_IdUsuario(idUsuarioLogado, idUsuarioLogado);
        if (matches.isEmpty()) {
            return List.of();
        }

        // Fotos dos "outros" participantes em 2 consultas (atletas + marcas), sem N+1.
        Set<Long> idsOutros = new HashSet<>();
        for (Match m : matches) {
            idsOutros.add(m.getUsuarioA().getIdUsuario().equals(idUsuarioLogado)
                    ? m.getUsuarioB().getIdUsuario()
                    : m.getUsuarioA().getIdUsuario());
        }
        Map<Long, String> fotos = new HashMap<>();
        for (PerfilAtleta p : perfilAtletaRepository.findByUsuarioIdIn(idsOutros)) {
            if (p.getFotoUrl() != null && !p.getFotoUrl().isBlank()) fotos.put(p.getUsuarioId(), p.getFotoUrl());
        }
        for (PerfilMarca p : perfilMarcaRepository.findByUsuarioIdIn(idsOutros)) {
            if (p.getLogoUrl() != null && !p.getLogoUrl().isBlank()) fotos.put(p.getUsuarioId(), p.getLogoUrl());
        }

        return matches.stream()
                .map(match -> new DadosDetalhamentoMatch(match, idUsuarioLogado, fotos))
                .collect(Collectors.toList());
    }
}
