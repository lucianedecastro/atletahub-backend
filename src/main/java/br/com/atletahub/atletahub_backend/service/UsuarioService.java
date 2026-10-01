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

import java.util.List;

@Service
public class UsuarioService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

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
