package br.com.atletahub.atletahub_backend.service;

import br.com.atletahub.atletahub_backend.dto.usuario.DadosRegistroUsuario;
import br.com.atletahub.atletahub_backend.model.TipoUsuario;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.model.mongo.PerfilVitrine; // Import do Modelo Mongo
import br.com.atletahub.atletahub_backend.repository.PerfilVitrineRepository; // Import do Repo Mongo
import br.com.atletahub.atletahub_backend.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.List;

@Service
public class UsuarioService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    // Idade mínima para ter conta. Menores só poderão entrar no futuro, via perfil administrado por responsável.
    public static final int IDADE_MINIMA = 18;

    // Versão dos Termos/Política aceitos no cadastro. Mude quando os textos mudarem.
    public static final String VERSAO_TERMOS = "2026-10";

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // Services para tabelas SQL (Dados básicos)
    private final PerfilAtletaService perfilAtletaService;
    private final PerfilMarcaService perfilMarcaService;

    // Repository do MongoDB (Vitrine de Fotos/Videos)
    private final PerfilVitrineRepository perfilVitrineRepository;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          PerfilAtletaService perfilAtletaService,
                          PerfilMarcaService perfilMarcaService,
                          PerfilVitrineRepository perfilVitrineRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.perfilAtletaService = perfilAtletaService;
        this.perfilMarcaService = perfilMarcaService;
        this.perfilVitrineRepository = perfilVitrineRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Mensagem genérica: não devolve o e-mail digitado (LGPD / evita enumeração de contas).
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

    @Transactional
    public Usuario registrarUsuario(@Valid DadosRegistroUsuario dados) {
        // LGPD: e-mail não vai para o log.
        logger.info("Iniciando registro de usuário (tipo={})", dados.tipoUsuario());

        // Ignora maiúsculas/minúsculas para não criar "Ana@x.com" e "ana@x.com" como contas diferentes.
        if (usuarioRepository.existsByEmailIgnoreCase(dados.email())) {
            logger.warn("Tentativa de cadastro com e-mail já existente");
            throw new IllegalArgumentException("Email já cadastrado.");
        }

        // Regra de idade validada ANTES de gravar qualquer coisa (a data não vai para o log).
        validarMaioridade(dados.dataNascimento());

        String senhaHash = passwordEncoder.encode(dados.senha());
        TipoUsuario tipoUsuarioEnum = TipoUsuario.valueOf(dados.tipoUsuario().toUpperCase());

        // Idioma: se não vier no DTO, usa "pt".
        String idiomaDefinido = (dados.idioma() != null && !dados.idioma().isBlank())
                ? dados.idioma().trim()
                : "pt";

        Usuario novoUsuario = new Usuario(
                dados.nome().trim(),
                dados.email(),
                senhaHash,
                tipoUsuarioEnum,
                idiomaDefinido
        );

        // Antes cidade/estado eram exigidos no cadastro, mas descartados. Agora são salvos.
        novoUsuario.setCidade(dados.cidade().trim());
        novoUsuario.setEstado(dados.estado().trim());

        // Maioridade e aceite dos termos (data e versão) ficam registrados na conta.
        novoUsuario.setDataNascimento(dados.dataNascimento());
        novoUsuario.setTermosAceitosEm(Instant.now());
        novoUsuario.setTermosVersao(VERSAO_TERMOS);

        // 1. Salva no PostgreSQL (Login e Auth)
        Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);
        logger.info("Usuário salvo no Postgres com ID: {}", usuarioSalvo.getIdUsuario());

        // 2. Cria os perfis dependendo do tipo
        if (usuarioSalvo.getTipoUsuario() == TipoUsuario.ATLETA) {
            perfilAtletaService.criarPerfilAtletaInicial(usuarioSalvo);
            criarVitrineMongo(usuarioSalvo);
        } else if (usuarioSalvo.getTipoUsuario() == TipoUsuario.MARCA) {
            perfilMarcaService.criarPerfilMarcaInicial(usuarioSalvo);
        }

        logger.info("Registro do usuário {} concluído com sucesso.", usuarioSalvo.getIdUsuario());
        return usuarioSalvo;
    }

    /**
     * Rejeita datas impossíveis e menores de {@link #IDADE_MINIMA} anos.
     * Lança IllegalArgumentException (vira 400 com a mensagem para a pessoa).
     */
    public static void validarMaioridade(LocalDate nascimento) {
        if (nascimento == null) {
            throw new IllegalArgumentException("A data de nascimento é obrigatória.");
        }
        LocalDate hoje = LocalDate.now(FUSO);
        if (nascimento.isAfter(hoje) || nascimento.getYear() < 1900) {
            throw new IllegalArgumentException("Data de nascimento inválida.");
        }
        if (Period.between(nascimento, hoje).getYears() < IDADE_MINIMA) {
            throw new IllegalArgumentException(
                    "É preciso ter " + IDADE_MINIMA + " anos ou mais para criar uma conta no AtletaHub.");
        }
    }

    /**
     * Contas criadas antes da V7 informam a data aqui, no primeiro acesso.
     * A data não pode ser trocada depois (evita "corrigir" a idade para passar na regra).
     * Devolve 422 (e não 401/403, que deslogariam o usuário no front).
     */
    @Transactional
    public void informarDataNascimento(Long idUsuario, LocalDate nascimento) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        if (usuario.getDataNascimento() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A data de nascimento já foi informada.");
        }

        try {
            validarMaioridade(nascimento);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        }

        usuario.setDataNascimento(nascimento);
        usuarioRepository.save(usuario);
        logger.info("Data de nascimento registrada para o usuário ID: {}", idUsuario);
    }

    // Método auxiliar para criar o documento no Mongo
    private void criarVitrineMongo(Usuario usuario) {
        try {
            PerfilVitrine vitrine = new PerfilVitrine(usuario.getIdUsuario());
            vitrine.setBiografiaCompleta("Bem-vindo! Complete seu perfil adicionando fotos e vídeos.");
            perfilVitrineRepository.save(vitrine);
            logger.info("Vitrine MongoDB criada para usuário ID: {}", usuario.getIdUsuario());
        } catch (Exception e) {
            // A vitrine também é criada sob demanda (VitrineService), então a falha aqui não derruba o cadastro.
            logger.error("Erro ao criar vitrine no MongoDB para usuário ID: " + usuario.getIdUsuario(), e);
        }
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
    }

    /**
     * Lista usuários de um tipo. Só ATLETA e MARCA podem ser listados por quem não é ADMIN
     * (antes qualquer logado conseguia listar os administradores).
     */
    public List<Usuario> buscarPorTipo(String tipoUsuario, Usuario solicitante) {
        TipoUsuario tipo;
        try {
            tipo = TipoUsuario.valueOf(tipoUsuario.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Tipo de usuário inválido.");
        }

        boolean solicitanteEhAdmin = solicitante != null && solicitante.getTipoUsuario() == TipoUsuario.ADMIN;
        if (tipo == TipoUsuario.ADMIN && !solicitanteEhAdmin) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhum usuário encontrado.");
        }

        return usuarioRepository.findByTipoUsuario(tipo);
    }
}
