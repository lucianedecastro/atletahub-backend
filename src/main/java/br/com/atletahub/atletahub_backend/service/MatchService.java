package br.com.atletahub.atletahub_backend.service;

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

@Service
public class MatchService {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private InteresseRepository interesseRepository;

    /**
     * Verifica se um match deve ser gerado após um novo interesse ser registrado.
     * @param origem O usuário que expressou o interesse.
     * @param destino O usuário que recebeu o interesse.
     * @param tipoInteresse O tipo de interesse registrado (CURTIR, SUPER_CURTIR).
     * @return O objeto Match criado, se houver, ou null.
     */
    @Transactional
    public Match verificarEGerarMatch(Usuario origem, Usuario destino, TipoInteresse tipoInteresse) {
        // Ordena os IDs para garantir consistência na busca e criação de matches

        Long idUsuarioA = Math.min(origem.getIdUsuario(), destino.getIdUsuario());
        Long idUsuarioB = Math.max(origem.getIdUsuario(), destino.getIdUsuario());

        // 1. Verificar se um match já existe entre esses dois usuários

        Optional<Match> matchExistente = matchRepository.findByUsuarioA_IdUsuarioAndUsuarioB_IdUsuario(idUsuarioA, idUsuarioB);
        if (matchExistente.isPresent()) {
            return null; // Match já existe, não precisa criar um novo
        }

        Match novoMatch = null;

        // Lógica para SUPER_CURTIR (pode gerar um SUPER_MATCH imediatamente ou sob condições)
        if (tipoInteresse == TipoInteresse.SUPER_CURTIR) {
            novoMatch = new Match(origem, destino, TipoMatch.SUPER_MATCH);
            return matchRepository.save(novoMatch);
        }

        // Lógica para CURTIR e verificar reciprocidade
        if (tipoInteresse == TipoInteresse.CURTIR) {
            // Verifica se o destino já havia curtido a origem

            Optional<Interesse> interesseReciproco = interesseRepository.findByOrigem_IdUsuarioAndDestino_IdUsuario(destino.getIdUsuario(), origem.getIdUsuario());

            if (interesseReciproco.isPresent() && interesseReciproco.get().getTipoInteresse() == TipoInteresse.CURTIR) {
                // Se o interesse recíproco for um CURTIR, então é um MATCH RECIPROCO
                novoMatch = new Match(origem, destino, TipoMatch.RECIPROCO);
                return matchRepository.save(novoMatch);
            }
        }
        return null; // Nenhum match gerado
    }

    // Método para buscar um match específico (útil para o Controller)
    public Match buscarMatchPorId(Long idMatch) {
        return matchRepository.findById(idMatch)
                .orElseThrow(() -> new RuntimeException("Match não encontrado."));
    }

    // Método para listar todos os matches de um usuário
    public List<Match> listarMatchesDoUsuario(Long idUsuario) {
        // Passa o mesmo ID duas vezes para buscar tanto em usuarioA quanto em usuarioB

        return matchRepository.findByUsuarioA_IdUsuarioOrUsuarioB_IdUsuario(idUsuario, idUsuario);
    }
}