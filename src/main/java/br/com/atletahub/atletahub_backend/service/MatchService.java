package br.com.atletahub.atletahub_backend.service;

import br.com.atletahub.atletahub_backend.dto.match.DadosDetalhamentoMatch; // Importar o novo DTO
import br.com.atletahub.atletahub_backend.enums.TipoInteresse;
import br.com.atletahub.atletahub_backend.enums.TipoMatch;
import br.com.atletahub.atletahub_backend.model.Interesse;
import br.com.atletahub.atletahub_backend.model.Match;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.repository.InteresseRepository;
import br.com.atletahub.atletahub_backend.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MatchService {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private InteresseRepository interesseRepository;


    @Transactional
    public Match verificarEGerarMatch(Usuario origem, Usuario destino, TipoInteresse tipoInteresse) {

        Long idUsuarioA = Math.min(origem.getIdUsuario(), destino.getIdUsuario());
        Long idUsuarioB = Math.max(origem.getIdUsuario(), destino.getIdUsuario());


        Optional<Match> matchExistente = matchRepository.findByUsuarioA_IdUsuarioAndUsuarioB_IdUsuario(idUsuarioA, idUsuarioB);
        if (matchExistente.isPresent()) {
            return null;
        }

        Match novoMatch = null;


        if (tipoInteresse == TipoInteresse.SUPER_CURTIR) {
            novoMatch = new Match(origem, destino, TipoMatch.SUPER_MATCH);
            return matchRepository.save(novoMatch);
        }


        if (tipoInteresse == TipoInteresse.CURTIR) {

            Optional<Interesse> interesseReciproco = interesseRepository.findByOrigem_IdUsuarioAndDestino_IdUsuario(destino.getIdUsuario(), origem.getIdUsuario());

            if (interesseReciproco.isPresent() && interesseReciproco.get().getTipoInteresse() == TipoInteresse.CURTIR) {

                novoMatch = new Match(origem, destino, TipoMatch.RECIPROCO);
                return matchRepository.save(novoMatch);
            }
        }
        return null;
    }

    public Match buscarMatchPorId(Long idMatch) {
        return matchRepository.findById(idMatch)
                .orElseThrow(() -> new RuntimeException("Match não encontrado."));
    }


    public List<DadosDetalhamentoMatch> listarMatchesDoUsuario(Long idUsuarioLogado) {
        List<Match> matches = matchRepository.findByUsuarioA_IdUsuarioOrUsuarioB_IdUsuario(idUsuarioLogado, idUsuarioLogado);
        return matches.stream()
                .map(match -> new DadosDetalhamentoMatch(match, idUsuarioLogado))
                .collect(Collectors.toList());
    }
}