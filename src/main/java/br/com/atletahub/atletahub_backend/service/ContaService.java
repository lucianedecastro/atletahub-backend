package br.com.atletahub.atletahub_backend.service;

import br.com.atletahub.atletahub_backend.model.StatusConta;
import br.com.atletahub.atletahub_backend.model.TipoUsuario;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.repository.PerfilVitrineRepository;
import br.com.atletahub.atletahub_backend.repository.RedefinicaoSenhaRepository;
import br.com.atletahub.atletahub_backend.repository.UsuarioRepository;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Encerramento de conta (exclusão): usado pelo pedido da própria pessoa e pelo painel admin.
 * - Anonimiza a conta e apaga os dados pessoais do perfil e a vitrine (Mongo).
 * - Apaga no Cloudinary as fotos, os vídeos, a logo e a foto de perfil (depois que o banco confirma).
 * - Mensagens trocadas ficam, sem o nome da pessoa (a outra parte da conversa também tem direito a elas).
 * - O registro do aceite dos termos (data e versão) é mantido como prova.
 * - Envia um e-mail de confirmação para o endereço que a conta tinha.
 */
@Service
public class ContaService {

    private static final Logger logger = LoggerFactory.getLogger(ContaService.class);

    private static final int MAXIMO_DE_ERROS = 5;
    private static final Duration TRAVA = Duration.ofMinutes(15);

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PerfilVitrineRepository perfilVitrineRepository;
    @Autowired private RedefinicaoSenhaRepository redefinicaoSenhaRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private CloudinaryService cloudinaryService;
    @Autowired private EmailService emailService;

    // Apagar arquivos na nuvem leva alguns segundos: roda em segundo plano, sem segurar a resposta.
    private final ExecutorService limpezaNaNuvem = Executors.newSingleThreadExecutor(tarefa -> {
        Thread fio = new Thread(tarefa, "limpeza-cloudinary");
        fio.setDaemon(true);
        return fio;
    });

    // Erros de senha na tela de exclusão, por conta (em memória; reiniciar o servidor zera).
    private static final class Tentativas {
        int erros;
        Instant travadoAte;
    }

    private final Map<Long, Tentativas> tentativas = new ConcurrentHashMap<>();

    // =====================================================
    // PEDIDO DA PRÓPRIA PESSOA
    // =====================================================
    @Transactional
    public void excluirPelaPropriaPessoa(Long idUsuario, String emailDigitado, String senhaDigitada) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada."));

        if (usuario.getTipoUsuario() == TipoUsuario.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Contas de administrador não podem ser excluídas por aqui.");
        }
        if (usuario.getStatus() != StatusConta.ATIVA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta conta não está ativa.");
        }

        Tentativas t = tentativas.computeIfAbsent(idUsuario, id -> new Tentativas());
        synchronized (t) {
            Instant agora = Instant.now();
            if (t.travadoAte != null && agora.isBefore(t.travadoAte)) {
                long minutos = Math.max(1, Duration.between(agora, t.travadoAte).toMinutes());
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Muitas tentativas. Tente de novo em " + minutos + " min.");
            }

            boolean emailConfere = emailDigitado != null
                    && emailDigitado.trim().equalsIgnoreCase(usuario.getEmail());
            boolean senhaConfere = senhaDigitada != null
                    && passwordEncoder.matches(senhaDigitada, usuario.getPassword());

            if (!emailConfere || !senhaConfere) {
                t.erros++;
                if (t.erros >= MAXIMO_DE_ERROS) {
                    t.travadoAte = agora.plus(TRAVA);
                    t.erros = 0;
                    logger.warn("Exclusão de conta travada por {} min após erros seguidos", TRAVA.toMinutes());
                }
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail ou senha não conferem.");
            }
            t.erros = 0;
            t.travadoAte = null;
        }

        encerrar(usuario);
        tentativas.remove(idUsuario);
        logger.info("Conta {} excluída a pedido da própria pessoa", idUsuario);
    }

    // =====================================================
    // ENCERRAMENTO (também chamado pelo painel admin)
    // =====================================================
    @Transactional
    public void encerrar(Usuario alvo) {
        Long id = alvo.getIdUsuario();
        String emailOriginal = alvo.getEmail();
        String nomeOriginal = alvo.getNome();

        // Primeiro anota onde estão os arquivos; depois de anonimizar os endereços se perdem.
        Set<String> arquivos = coletarArquivos(id);

        alvo.setNome("Conta removida");
        alvo.setEmail("removido-" + id + "@atletahub.invalid");
        alvo.setSenha(passwordEncoder.encode(UUID.randomUUID().toString()));
        alvo.setCidade(null);
        alvo.setEstado(null);
        alvo.setDataNascimento(null);
        alvo.setStatus(StatusConta.ENCERRADA);
        alvo.setMotivoSuspensao(null);
        alvo.setStatusAlteradoEm(Instant.now());
        // O registro do aceite dos termos (termosAceitosEm e termosVersao) é mantido como prova.
        usuarioRepository.save(alvo);

        jdbc.update("update perfil_atleta set idade = null, posicao = null, altura = null, peso = null, "
                + "data_nascimento = null, telefone_contato = null, observacoes = null, midiakit_url = null, "
                + "competicoes_titulos = null, redes_social = null, historico = null, foto_url = null "
                + "where id_usuario = ?", id);
        jdbc.update("update perfil_marca set produto = null, tempo_mercado = null, atletas_patrocinados = null, "
                + "tipo_investimento = null, redes_social = null, logo_url = null where id_usuario = ?", id);

        perfilVitrineRepository.findByUsuarioId(id).ifPresent(perfilVitrineRepository::delete);

        // Links de "esqueci minha senha" ainda abertos deixam de valer.
        redefinicaoSenhaRepository.invalidarAbertos(id, Instant.now());

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    aposEncerrar(arquivos, emailOriginal, nomeOriginal);
                }
            });
        } else {
            aposEncerrar(arquivos, emailOriginal, nomeOriginal);
        }
    }

    // Só roda depois que o banco confirmou: se a gravação falhar, nada é apagado nem enviado.
    private void aposEncerrar(Set<String> arquivos, String emailOriginal, String nomeOriginal) {
        if (!arquivos.isEmpty()) {
            limpezaNaNuvem.submit(() -> cloudinaryService.apagarPorUrls(arquivos));
        }
        String nome = primeiroNome(nomeOriginal);
        emailService.enviarEmSegundoPlano(
                emailOriginal,
                "Sua conta no AtletaHub foi excluída",
                montarHtmlDaConfirmacao(nome),
                montarTextoDaConfirmacao(nome));
    }

    // Todos os endereços de arquivos da pessoa: vitrine (fotos e vídeos), logo da marca, foto de perfil e mídia kit.
    private Set<String> coletarArquivos(Long id) {
        Set<String> urls = new LinkedHashSet<>();

        perfilVitrineRepository.findByUsuarioId(id).ifPresent(vitrine -> {
            if (vitrine.getFotos() != null) urls.addAll(vitrine.getFotos());
            if (vitrine.getVideos() != null) urls.addAll(vitrine.getVideos());
        });

        urls.addAll(colunaDeTexto("select foto_url from perfil_atleta where id_usuario = ?", id));
        urls.addAll(colunaDeTexto("select midiakit_url from perfil_atleta where id_usuario = ?", id));
        urls.addAll(colunaDeTexto("select logo_url from perfil_marca where id_usuario = ?", id));

        urls.removeIf(u -> u == null || u.isBlank());
        return urls;
    }

    private List<String> colunaDeTexto(String sql, Object... args) {
        return jdbc.queryForList(sql, String.class, args);
    }

    // =====================================================
    // TEXTOS DO E-MAIL
    // =====================================================
    private static String primeiroNome(String nomeCompleto) {
        if (nomeCompleto == null || nomeCompleto.isBlank()) {
            return "";
        }
        return nomeCompleto.trim().split("\\s+")[0];
    }

    private static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String saudacao(String nome) {
        return nome.isEmpty() ? "Olá." : "Olá, " + nome + ".";
    }

    private static String montarTextoDaConfirmacao(String nome) {
        return saudacao(nome) + "\n\n"
                + "Sua conta no AtletaHub foi excluída.\n\n"
                + "Apagamos seu nome, seu e-mail, os dados do seu perfil e os arquivos que você enviou (fotos e vídeos).\n\n"
                + "As mensagens que você trocou com outras pessoas continuam visíveis para elas, sem o seu nome.\n\n"
                + "Equipe AtletaHub";
    }

    private static String montarHtmlDaConfirmacao(String nome) {
        return "<div style=\"font-family:Arial,Helvetica,sans-serif;font-size:16px;line-height:1.5;color:#0A1633;max-width:480px\">"
                + "<p>" + escapar(saudacao(nome)) + "</p>"
                + "<p>Sua conta no AtletaHub foi excluída.</p>"
                + "<p>Apagamos seu nome, seu e-mail, os dados do seu perfil e os arquivos que você enviou (fotos e vídeos).</p>"
                + "<p>As mensagens que você trocou com outras pessoas continuam visíveis para elas, sem o seu nome.</p>"
                + "<p>Equipe AtletaHub</p>"
                + "</div>";
    }

    @PreDestroy
    void encerrarFila() {
        limpezaNaNuvem.shutdown();
    }
}
