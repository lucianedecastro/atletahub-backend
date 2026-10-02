package br.com.atletahub.atletahub_backend.service;

import br.com.atletahub.atletahub_backend.model.Bloqueio;
import br.com.atletahub.atletahub_backend.model.PerfilAtleta;
import br.com.atletahub.atletahub_backend.model.PerfilMarca;
import br.com.atletahub.atletahub_backend.model.StatusConta;
import br.com.atletahub.atletahub_backend.model.TipoUsuario;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.repository.BloqueioRepository;
import br.com.atletahub.atletahub_backend.repository.PerfilAtletaRepository;
import br.com.atletahub.atletahub_backend.repository.PerfilMarcaRepository;
import br.com.atletahub.atletahub_backend.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Bloqueio entre atleta e marca.
 * Efeito: os dois deixam de aparecer um para o outro (Descobrir, interesses, perfil de quem bloqueou)
 * e a conversa que já existia fica congelada (histórico guardado, sem mensagens novas).
 * A pessoa bloqueada não é avisada. Desbloquear desfaz tudo.
 */
@Service
public class BloqueioService {

    private static final Logger logger = LoggerFactory.getLogger(BloqueioService.class);

    // Limite para evitar abuso (lista infinita de bloqueios).
    private static final int MAXIMO_DE_BLOQUEIOS = 500;

    @Autowired private BloqueioRepository bloqueioRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PerfilAtletaRepository perfilAtletaRepository;
    @Autowired private PerfilMarcaRepository perfilMarcaRepository;

    @Transactional
    public void bloquear(Usuario logado, Long idAlvo) {
        if (logado.getIdUsuario().equals(idAlvo)) {
            throw new IllegalArgumentException("Você não pode bloquear a si mesmo.");
        }
        validarPapel(logado);

        // Erro único (404) para conta que não existe, não está ativa, é admin ou é do mesmo tipo.
        Usuario alvo = usuarioRepository.findById(idAlvo)
                .filter(u -> u.getStatus() == StatusConta.ATIVA)
                .filter(u -> (logado.getTipoUsuario() == TipoUsuario.ATLETA && u.getTipoUsuario() == TipoUsuario.MARCA)
                        || (logado.getTipoUsuario() == TipoUsuario.MARCA && u.getTipoUsuario() == TipoUsuario.ATLETA))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        if (bloqueioRepository.existsByIdBloqueadorAndIdBloqueado(logado.getIdUsuario(), alvo.getIdUsuario())) {
            return; // já estava bloqueado: mesmo resultado
        }
        if (bloqueioRepository.idsQueEuBloqueei(logado.getIdUsuario()).size() >= MAXIMO_DE_BLOQUEIOS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Você chegou ao limite de pessoas bloqueadas.");
        }

        bloqueioRepository.save(new Bloqueio(logado.getIdUsuario(), alvo.getIdUsuario()));
        logger.info("Bloqueio criado (bloqueador={}, bloqueado={})", logado.getIdUsuario(), alvo.getIdUsuario());
    }

    @Transactional
    public void desbloquear(Usuario logado, Long idAlvo) {
        bloqueioRepository.findByIdBloqueadorAndIdBloqueado(logado.getIdUsuario(), idAlvo)
                .ifPresent(b -> {
                    bloqueioRepository.delete(b);
                    logger.info("Bloqueio removido (bloqueador={}, bloqueado={})", logado.getIdUsuario(), idAlvo);
                });
    }

    /** Pessoas que o usuário bloqueou, da mais recente para a mais antiga. */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listar(Usuario logado) {
        List<Bloqueio> bloqueios = bloqueioRepository.findByIdBloqueadorOrderByCriadoEmDesc(logado.getIdUsuario());
        if (bloqueios.isEmpty()) {
            return List.of();
        }

        Set<Long> ids = new HashSet<>();
        for (Bloqueio b : bloqueios) ids.add(b.getIdBloqueado());

        Map<Long, Usuario> usuarios = new HashMap<>();
        for (Usuario u : usuarioRepository.findAllById(ids)) usuarios.put(u.getIdUsuario(), u);

        Map<Long, String> fotos = new HashMap<>();
        for (PerfilAtleta p : perfilAtletaRepository.findByUsuarioIdIn(ids)) {
            if (p.getFotoUrl() != null && !p.getFotoUrl().isBlank()) fotos.put(p.getUsuarioId(), p.getFotoUrl());
        }
        for (PerfilMarca p : perfilMarcaRepository.findByUsuarioIdIn(ids)) {
            if (p.getLogoUrl() != null && !p.getLogoUrl().isBlank()) fotos.put(p.getUsuarioId(), p.getLogoUrl());
        }

        List<Map<String, Object>> saida = new ArrayList<>();
        for (Bloqueio b : bloqueios) {
            Usuario u = usuarios.get(b.getIdBloqueado());
            if (u == null) continue;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("idUsuario", u.getIdUsuario());
            item.put("nome", u.getNome());
            item.put("tipoUsuario", u.getTipoUsuario().name());
            item.put("fotoUrl", fotos.get(u.getIdUsuario()));
            item.put("bloqueadoEm", b.getCriadoEm().toString());
            saida.add(item);
        }
        return saida;
    }

    /** Existe bloqueio entre as duas pessoas, em qualquer direção. */
    @Transactional(readOnly = true)
    public boolean existeEntre(Long a, Long b) {
        return bloqueioRepository.existeEntre(a, b);
    }

    /** O outro usuário bloqueou o logado? (quem foi bloqueado não vê o perfil de quem bloqueou) */
    @Transactional(readOnly = true)
    public boolean fuiBloqueadoPor(Long idOutro, Long idEu) {
        return bloqueioRepository.existsByIdBloqueadorAndIdBloqueado(idOutro, idEu);
    }

    /** Todos que o usuário não deve ver (e que não devem vê-lo): quem ele bloqueou e quem o bloqueou. */
    @Transactional(readOnly = true)
    public Set<Long> idsComBloqueio(Long idUsuario) {
        Set<Long> ids = new HashSet<>(bloqueioRepository.idsQueEuBloqueei(idUsuario));
        ids.addAll(bloqueioRepository.idsQueMeBloquearam(idUsuario));
        return ids;
    }

    private void validarPapel(Usuario logado) {
        if (logado.getTipoUsuario() != TipoUsuario.ATLETA && logado.getTipoUsuario() != TipoUsuario.MARCA) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
        }
    }
}
