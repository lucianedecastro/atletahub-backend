package br.com.atletahub.atletahub_backend.service;

import br.com.atletahub.atletahub_backend.enums.StatusDenuncia;
import br.com.atletahub.atletahub_backend.model.Denuncia;
import br.com.atletahub.atletahub_backend.model.StatusConta;
import br.com.atletahub.atletahub_backend.model.TipoUsuario;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.model.mongo.PerfilVitrine;
import br.com.atletahub.atletahub_backend.repository.DenunciaRepository;
import br.com.atletahub.atletahub_backend.repository.PerfilVitrineRepository;
import br.com.atletahub.atletahub_backend.repository.UsuarioRepository;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Regras do painel admin: números, usuários, suspensão, encerramento (anonimização),
 * remoção de mídia e análise de denúncias. Nada daqui é exposto sem o papel ADMIN.
 */
@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private DenunciaRepository denunciaRepository;
    @Autowired private PerfilVitrineRepository perfilVitrineRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private ContaService contaService;
    @Autowired private CloudinaryService cloudinaryService;

    // =====================================================
    // NÚMEROS
    // =====================================================
    public Map<String, Object> resumo() {
        Instant h7 = Instant.now().minus(7, ChronoUnit.DAYS);
        Instant h30 = Instant.now().minus(30, ChronoUnit.DAYS);
        Timestamp t7 = Timestamp.valueOf(LocalDateTime.now().minusDays(7));
        Timestamp t30 = Timestamp.valueOf(LocalDateTime.now().minusDays(30));

        Map<String, Object> r = new LinkedHashMap<>();

        Map<String, Object> usuarios = new LinkedHashMap<>();
        usuarios.put("atletasAtivos", usuarioRepository.countByTipoUsuarioAndStatus(TipoUsuario.ATLETA, StatusConta.ATIVA));
        usuarios.put("marcasAtivas", usuarioRepository.countByTipoUsuarioAndStatus(TipoUsuario.MARCA, StatusConta.ATIVA));
        usuarios.put("atletasSuspensos", usuarioRepository.countByTipoUsuarioAndStatus(TipoUsuario.ATLETA, StatusConta.SUSPENSA));
        usuarios.put("marcasSuspensas", usuarioRepository.countByTipoUsuarioAndStatus(TipoUsuario.MARCA, StatusConta.SUSPENSA));
        usuarios.put("novosUltimos7Dias", usuarioRepository.countByTermosAceitosEmAfter(h7));
        usuarios.put("novosUltimos30Dias", usuarioRepository.countByTermosAceitosEmAfter(h30));
        usuarios.put("contasAntigasSemDataDeNascimento", contar(
                "select count(*) from usuario where data_nascimento is null and tipo_usuario <> 'ADMIN' and status = 'ATIVA'"));
        r.put("usuarios", usuarios);

        Map<String, Object> atividade = new LinkedHashMap<>();
        atividade.put("interesses", contar("select count(*) from interesse"));
        atividade.put("matches", contar("select count(*) from match_table"));
        atividade.put("matchesUltimos7Dias", contar("select count(*) from match_table where data_match > ?", t7));
        atividade.put("mensagens", contar("select count(*) from mensagem"));
        atividade.put("mensagensUltimos7Dias", contar("select count(*) from mensagem where data_envio > ?", t7));
        r.put("atividade", atividade);

        Map<String, Object> traducao = new LinkedHashMap<>();
        traducao.put("traducoes", contar("select count(*) from mensagem_traducao"));
        traducao.put("traducoesUltimos30Dias", contar("select count(*) from mensagem_traducao where data_traducao > ?", t30));
        // Caracteres do texto traduzido: serve de referência para o custo do AWS Translate.
        traducao.put("caracteresUltimos30Dias", contar(
                "select coalesce(sum(length(texto_traduzido)), 0) from mensagem_traducao where data_traducao > ?", t30));
        r.put("traducao", traducao);

        Map<String, Object> moderacao = new LinkedHashMap<>();
        moderacao.put("denunciasAbertas", denunciaRepository.countByStatus(StatusDenuncia.ABERTA));
        r.put("moderacao", moderacao);

        return r;
    }

    private long contar(String sql, Object... args) {
        Long v = jdbc.queryForObject(sql, Long.class, args);
        return v == null ? 0L : v;
    }

    // =====================================================
    // USUÁRIOS
    // =====================================================
    public Map<String, Object> listarUsuarios(String busca, TipoUsuario tipo, StatusConta status, int pagina, int tamanho) {
        String termo = busca == null ? "" : busca.trim().toLowerCase().replaceAll("[%_\\\\]", "");

        Specification<Usuario> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (tipo != null) p.add(cb.equal(root.get("tipoUsuario"), tipo));
            if (status != null) p.add(cb.equal(root.get("status"), status));
            if (!termo.isEmpty()) {
                String like = "%" + termo + "%";
                p.add(cb.or(
                        cb.like(cb.lower(root.get("nome")), like),
                        cb.like(cb.lower(root.get("email")), like)));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };

        Page<Usuario> resultado = usuarioRepository.findAll(spec,
                PageRequest.of(Math.max(0, pagina), limitar(tamanho), Sort.by(Sort.Direction.DESC, "idUsuario")));

        return pagina(resultado.getContent().stream().map(this::resumoUsuario).toList(), resultado);
    }

    public Map<String, Object> detalharUsuario(Long id) {
        Usuario u = buscar(id);
        Map<String, Object> r = resumoUsuario(u);
        r.put("idioma", u.getIdiomaPreferencia());
        r.put("motivoSuspensao", u.getMotivoSuspensao());
        r.put("statusAlteradoEm", u.getStatusAlteradoEm());
        r.put("termosAceitosEm", u.getTermosAceitosEm());
        r.put("termosVersao", u.getTermosVersao());
        // A data de nascimento não é mostrada: só se sabe se já foi informada.
        r.put("dataNascimentoInformada", u.getDataNascimento() != null);

        // Só campos de moderação: telefone e data de nascimento ficam de fora de propósito.
        if (u.getTipoUsuario() == TipoUsuario.ATLETA) {
            r.put("perfil", primeiro(jdbc.queryForList(
                    "select modalidade, posicao, observacoes, redes_social, historico, competicoes_titulos, foto_url "
                            + "from perfil_atleta where id_usuario = ?", id)));
        } else if (u.getTipoUsuario() == TipoUsuario.MARCA) {
            r.put("perfil", primeiro(jdbc.queryForList(
                    "select produto, tempo_mercado, atletas_patrocinados, tipo_investimento, redes_social, logo_url "
                            + "from perfil_marca where id_usuario = ?", id)));
        }

        Map<String, Object> vitrine = new LinkedHashMap<>();
        Optional<PerfilVitrine> v = perfilVitrineRepository.findByUsuarioId(id);
        vitrine.put("biografia", v.map(PerfilVitrine::getBiografiaCompleta).orElse(null));
        vitrine.put("fotos", v.map(PerfilVitrine::getFotos).orElse(List.of()));
        vitrine.put("videos", v.map(PerfilVitrine::getVideos).orElse(List.of()));
        r.put("vitrine", vitrine);

        r.put("denunciasRecebidas", denunciaRepository.findByIdDenunciadoOrderByCriadaEmDesc(id).stream()
                .map(d -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", d.getId());
                    m.put("motivo", d.getMotivo());
                    m.put("tipoAlvo", d.getTipoAlvo());
                    m.put("status", d.getStatus());
                    m.put("criadaEm", d.getCriadaEm());
                    return m;
                }).toList());
        return r;
    }

    @Transactional
    public void suspender(Long idAdmin, Long idAlvo, String motivo) {
        Usuario alvo = buscar(idAlvo);
        validarAlvoDeModeracao(idAdmin, alvo);
        if (alvo.getStatus() == StatusConta.ENCERRADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta conta já foi encerrada.");
        }
        alvo.setStatus(StatusConta.SUSPENSA);
        alvo.setMotivoSuspensao(motivo.trim());
        alvo.setStatusAlteradoEm(Instant.now());
        usuarioRepository.save(alvo);
        logger.info("Admin {} suspendeu a conta {}", idAdmin, idAlvo);
    }

    @Transactional
    public void reativar(Long idAdmin, Long idAlvo) {
        Usuario alvo = buscar(idAlvo);
        if (alvo.getStatus() != StatusConta.SUSPENSA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Só é possível reativar uma conta suspensa.");
        }
        alvo.setStatus(StatusConta.ATIVA);
        alvo.setMotivoSuspensao(null);
        alvo.setStatusAlteradoEm(Instant.now());
        usuarioRepository.save(alvo);
        logger.info("Admin {} reativou a conta {}", idAdmin, idAlvo);
    }

    /**
     * Atende pedido de exclusão (LGPD) feito por e-mail ao suporte: a mesma rotina da exclusão pela própria pessoa
     * (anonimiza a conta, apaga dados do perfil, a vitrine e os arquivos no Cloudinary; mensagens ficam).
     */
    @Transactional
    public void encerrar(Long idAdmin, Long idAlvo, String confirmarEmail) {
        Usuario alvo = buscar(idAlvo);
        validarAlvoDeModeracao(idAdmin, alvo);
        if (alvo.getStatus() == StatusConta.ENCERRADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta conta já foi encerrada.");
        }
        if (confirmarEmail == null || !confirmarEmail.trim().equalsIgnoreCase(alvo.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O e-mail digitado não confere com o da conta.");
        }

        contaService.encerrar(alvo);
        logger.info("Admin {} encerrou e anonimizou a conta {}", idAdmin, idAlvo);
    }

    public void removerMidia(Long idUsuario, String url) {
        buscar(idUsuario);
        PerfilVitrine vitrine = perfilVitrineRepository.findByUsuarioId(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Esta pessoa não tem vitrine."));
        boolean removeu = false;
        if (vitrine.getFotos() != null) removeu = vitrine.getFotos().remove(url) || removeu;
        if (vitrine.getVideos() != null) removeu = vitrine.getVideos().remove(url) || removeu;
        if (!removeu) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mídia não encontrada na vitrine.");
        }
        perfilVitrineRepository.save(vitrine);
        // Apaga também o arquivo no Cloudinary (se falhar, a mídia já saiu da vitrine e o erro fica no log).
        cloudinaryService.apagarPorUrls(List.of(url));
        logger.info("Mídia removida da vitrine do usuário {}", idUsuario);
    }

    // =====================================================
    // DENÚNCIAS
    // =====================================================
    public Map<String, Object> listarDenuncias(StatusDenuncia status, int pagina, int tamanho) {
        PageRequest pr = PageRequest.of(Math.max(0, pagina), limitar(tamanho), Sort.by(Sort.Direction.DESC, "criadaEm"));
        Page<Denuncia> resultado = status == null ? denunciaRepository.findAll(pr) : denunciaRepository.findByStatus(status, pr);
        return pagina(montarDenuncias(resultado.getContent()), resultado);
    }

    public Map<String, Object> detalharDenuncia(Long id) {
        Denuncia d = denunciaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Denúncia não encontrada."));
        return montarDenuncias(List.of(d)).get(0);
    }

    @Transactional
    public void resolverDenuncia(Long idAdmin, Long id, StatusDenuncia decisao, String nota,
                                 boolean suspenderDenunciado, String motivoSuspensao) {
        Denuncia d = denunciaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Denúncia não encontrada."));
        if (d.getStatus() != StatusDenuncia.ABERTA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta denúncia já foi analisada.");
        }
        if (decisao == StatusDenuncia.ABERTA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Escolha PROCEDENTE ou IMPROCEDENTE.");
        }

        d.setStatus(decisao);
        d.setNotaAdmin(nota == null || nota.isBlank() ? null : nota.trim());
        d.setResolvidaEm(Instant.now());
        d.setResolvidaPor(idAdmin);
        denunciaRepository.save(d);

        if (suspenderDenunciado && decisao == StatusDenuncia.PROCEDENTE) {
            String motivo = (motivoSuspensao == null || motivoSuspensao.isBlank())
                    ? "Denúncia procedente (#" + id + ")" : motivoSuspensao;
            suspender(idAdmin, d.getIdDenunciado(), motivo);
        }
        logger.info("Admin {} resolveu a denúncia {} como {}", idAdmin, id, decisao);
    }

    // =====================================================
    // APOIO
    // =====================================================
    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
    }

    private void validarAlvoDeModeracao(Long idAdmin, Usuario alvo) {
        if (alvo.getTipoUsuario() == TipoUsuario.ADMIN || alvo.getIdUsuario().equals(idAdmin)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não é possível aplicar esta ação a uma conta de administrador.");
        }
    }

    private Map<String, Object> resumoUsuario(Usuario u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getIdUsuario());
        m.put("nome", u.getNome());
        m.put("email", u.getEmail());
        m.put("tipoUsuario", u.getTipoUsuario());
        m.put("status", u.getStatus());
        m.put("cidade", u.getCidade());
        m.put("estado", u.getEstado());
        return m;
    }

    private List<Map<String, Object>> montarDenuncias(List<Denuncia> lista) {
        Set<Long> ids = new HashSet<>();
        lista.forEach(d -> {
            ids.add(d.getIdDenunciante());
            ids.add(d.getIdDenunciado());
        });
        Map<Long, Usuario> usuarios = new HashMap<>();
        usuarioRepository.findAllById(ids).forEach(u -> usuarios.put(u.getIdUsuario(), u));

        List<Map<String, Object>> saida = new ArrayList<>();
        for (Denuncia d : lista) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", d.getId());
            m.put("status", d.getStatus());
            m.put("motivo", d.getMotivo());
            m.put("tipoAlvo", d.getTipoAlvo());
            m.put("descricao", d.getDescricao());
            m.put("referencia", d.getReferencia());
            m.put("trecho", d.getTrecho());
            m.put("criadaEm", d.getCriadaEm());
            m.put("resolvidaEm", d.getResolvidaEm());
            m.put("notaAdmin", d.getNotaAdmin());
            m.put("denunciante", pessoa(usuarios.get(d.getIdDenunciante()), d.getIdDenunciante()));
            m.put("denunciado", pessoa(usuarios.get(d.getIdDenunciado()), d.getIdDenunciado()));
            saida.add(m);
        }
        return saida;
    }

    private Map<String, Object> pessoa(Usuario u, Long id) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("nome", u != null ? u.getNome() : null);
        m.put("status", u != null ? u.getStatus() : null);
        return m;
    }

    private Map<String, Object> primeiro(List<Map<String, Object>> linhas) {
        return linhas.isEmpty() ? null : linhas.get(0);
    }

    private int limitar(int tamanho) {
        return Math.min(100, Math.max(1, tamanho));
    }

    private Map<String, Object> pagina(List<?> conteudo, Page<?> p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("conteudo", conteudo);
        m.put("pagina", p.getNumber());
        m.put("tamanho", p.getSize());
        m.put("total", p.getTotalElements());
        m.put("totalPaginas", p.getTotalPages());
        return m;
    }
}
