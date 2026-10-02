package br.com.atletahub.atletahub_backend.service;

import br.com.atletahub.atletahub_backend.model.RedefinicaoSenha;
import br.com.atletahub.atletahub_backend.model.StatusConta;
import br.com.atletahub.atletahub_backend.model.TipoUsuario;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.repository.RedefinicaoSenhaRepository;
import br.com.atletahub.atletahub_backend.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

/**
 * "Esqueci minha senha": o usuário pede um link por e-mail, o link vale 30 minutos e só funciona uma vez.
 * - Só o hash do código fica no banco.
 * - A resposta ao pedido é sempre a mesma, exista ou não a conta (não revela quem tem cadastro).
 * - Contas de admin e contas suspensas ou encerradas não recebem link.
 * - No máximo 3 pedidos por conta por hora.
 */
@Service
public class RecuperacaoSenhaService {

    private static final Logger logger = LoggerFactory.getLogger(RecuperacaoSenhaService.class);

    static final Duration VALIDADE_DO_LINK = Duration.ofMinutes(30);
    static final int MAXIMO_DE_PEDIDOS_POR_HORA = 3;
    static final int SENHA_MINIMA = 8;
    static final int SENHA_MAXIMA = 72; // limite do bcrypt

    public static final String MENSAGEM_LINK_INVALIDO = "Link inválido ou expirado. Peça um novo link.";

    private final SecureRandom aleatorio = new SecureRandom();

    // Endereço do site, usado no link do e-mail. Variável APP_FRONT_URL no Render (padrão: site oficial).
    @Value("${APP_FRONT_URL:https://www.atletahub.com.br}")
    private String enderecoDoSite;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RedefinicaoSenhaRepository redefinicaoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Transactional
    public void solicitar(String emailInformado) {
        if (emailInformado == null) {
            return;
        }
        String email = emailInformado.trim();
        if (email.isEmpty()) {
            return;
        }

        Optional<Usuario> achado = usuarioRepository.findByEmail(email);
        if (achado.isEmpty()) {
            logger.debug("Pedido de redefinição para e-mail sem conta");
            return;
        }

        Usuario usuario = achado.get();
        if (usuario.getTipoUsuario() == TipoUsuario.ADMIN || usuario.getStatus() != StatusConta.ATIVA) {
            logger.info("Pedido de redefinição ignorado (conta admin, suspensa ou encerrada)");
            return;
        }

        Instant agora = Instant.now();
        long pedidosNaUltimaHora = redefinicaoRepository
                .countByIdUsuarioAndCriadaEmAfter(usuario.getIdUsuario(), agora.minus(Duration.ofHours(1)));
        if (pedidosNaUltimaHora >= MAXIMO_DE_PEDIDOS_POR_HORA) {
            logger.info("Pedido de redefinição ignorado: limite por hora atingido");
            return;
        }

        redefinicaoRepository.apagarVencidosAntesDe(agora.minus(Duration.ofDays(7)));
        redefinicaoRepository.invalidarAbertos(usuario.getIdUsuario(), agora);

        String codigo = gerarCodigo();
        redefinicaoRepository.save(new RedefinicaoSenha(
                usuario.getIdUsuario(), hashDo(codigo), agora, agora.plus(VALIDADE_DO_LINK)));

        String link = enderecoSemBarraFinal() + "/redefinir-senha?token=" + codigo;
        String nome = primeiroNome(usuario.getNome());

        emailService.enviarEmSegundoPlano(
                usuario.getEmail(),
                "Redefinir sua senha no AtletaHub",
                montarHtmlDoLink(nome, link),
                montarTextoDoLink(nome, link));
    }

    @Transactional
    public void redefinir(String codigo, String novaSenha) {
        if (novaSenha == null || novaSenha.length() < SENHA_MINIMA || novaSenha.length() > SENHA_MAXIMA) {
            throw new IllegalArgumentException(
                    "A senha precisa ter entre " + SENHA_MINIMA + " e " + SENHA_MAXIMA + " caracteres.");
        }
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException(MENSAGEM_LINK_INVALIDO);
        }

        RedefinicaoSenha pedido = redefinicaoRepository.findByTokenHash(hashDo(codigo.trim()))
                .orElseThrow(() -> new IllegalArgumentException(MENSAGEM_LINK_INVALIDO));

        Instant agora = Instant.now();
        if (pedido.getUsadaEm() != null || pedido.getExpiraEm().isBefore(agora)) {
            throw new IllegalArgumentException(MENSAGEM_LINK_INVALIDO);
        }

        Usuario usuario = usuarioRepository.findById(pedido.getIdUsuario())
                .orElseThrow(() -> new IllegalArgumentException(MENSAGEM_LINK_INVALIDO));
        if (usuario.getTipoUsuario() == TipoUsuario.ADMIN || usuario.getStatus() != StatusConta.ATIVA) {
            throw new IllegalArgumentException(MENSAGEM_LINK_INVALIDO);
        }

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        // Gasta este link e qualquer outro que ainda estivesse aberto.
        redefinicaoRepository.invalidarAbertos(usuario.getIdUsuario(), agora);

        logger.info("Senha redefinida por link de e-mail");

        String nome = primeiroNome(usuario.getNome());
        emailService.enviarEmSegundoPlano(
                usuario.getEmail(),
                "Sua senha do AtletaHub foi alterada",
                montarHtmlDoAviso(nome),
                montarTextoDoAviso(nome));
    }

    // ---------------- código e hash ----------------

    private String gerarCodigo() {
        byte[] bytes = new byte[32];
        aleatorio.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hashDo(String codigo) {
        try {
            MessageDigest resumo = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(resumo.digest(codigo.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    private String enderecoSemBarraFinal() {
        String endereco = enderecoDoSite == null ? "" : enderecoDoSite.trim();
        while (endereco.endsWith("/")) {
            endereco = endereco.substring(0, endereco.length() - 1);
        }
        return endereco;
    }

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

    // ---------------- textos dos e-mails ----------------

    private static String montarTextoDoLink(String nome, String link) {
        return saudacao(nome) + "\n\n"
                + "Recebemos um pedido para redefinir a senha da sua conta no AtletaHub.\n\n"
                + "Para criar uma nova senha, abra este endereço:\n" + link + "\n\n"
                + "O link vale por 30 minutos e só pode ser usado uma vez.\n\n"
                + "Se você não pediu isso, ignore este e-mail. Sua senha continua a mesma.\n\n"
                + "Equipe AtletaHub";
    }

    private static String montarHtmlDoLink(String nome, String link) {
        String linkSeguro = escapar(link);
        return "<div style=\"font-family:Arial,Helvetica,sans-serif;font-size:16px;line-height:1.5;color:#0A1633;max-width:480px\">"
                + "<p>" + escapar(saudacao(nome)) + "</p>"
                + "<p>Recebemos um pedido para redefinir a senha da sua conta no AtletaHub.</p>"
                + "<p><a href=\"" + linkSeguro + "\" style=\"display:inline-block;background:#1646B5;color:#ffffff;"
                + "text-decoration:none;font-weight:bold;padding:12px 24px;border-radius:8px\">Criar nova senha</a></p>"
                + "<p>O link vale por 30 minutos e só pode ser usado uma vez.</p>"
                + "<p>Se o botão não funcionar, copie e cole este endereço no navegador:<br>"
                + "<span style=\"word-break:break-all\">" + linkSeguro + "</span></p>"
                + "<p>Se você não pediu isso, ignore este e-mail. Sua senha continua a mesma.</p>"
                + "<p>Equipe AtletaHub</p>"
                + "</div>";
    }

    private String montarTextoDoAviso(String nome) {
        return saudacao(nome) + "\n\n"
                + "A senha da sua conta no AtletaHub acabou de ser alterada.\n\n"
                + "Se foi você, não precisa fazer nada.\n"
                + "Se não foi você, crie uma nova senha agora: " + enderecoSemBarraFinal() + "/esqueci-senha\n\n"
                + "Equipe AtletaHub";
    }

    private String montarHtmlDoAviso(String nome) {
        String endereco = escapar(enderecoSemBarraFinal() + "/esqueci-senha");
        return "<div style=\"font-family:Arial,Helvetica,sans-serif;font-size:16px;line-height:1.5;color:#0A1633;max-width:480px\">"
                + "<p>" + escapar(saudacao(nome)) + "</p>"
                + "<p>A senha da sua conta no AtletaHub acabou de ser alterada.</p>"
                + "<p>Se foi você, não precisa fazer nada.</p>"
                + "<p>Se não foi você, <a href=\"" + endereco + "\">crie uma nova senha agora</a>.</p>"
                + "<p>Equipe AtletaHub</p>"
                + "</div>";
    }
}
