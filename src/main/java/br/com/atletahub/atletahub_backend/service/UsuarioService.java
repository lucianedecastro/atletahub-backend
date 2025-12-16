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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Email não encontrado: " + email));
    }

    @Transactional
    public Usuario registrarUsuario(@Valid DadosRegistroUsuario dados) {
        logger.info("Iniciando registro do usuário: {}", dados.email());

        if (usuarioRepository.findByEmail(dados.email()).isPresent()) {
            logger.warn("Email já cadastrado: {}", dados.email());
            throw new IllegalArgumentException("Email já cadastrado.");
        }

        String senhaHash = passwordEncoder.encode(dados.senha());
        TipoUsuario tipoUsuarioEnum = TipoUsuario.valueOf(dados.tipoUsuario().toUpperCase());

        // --- LÓGICA DE IDIOMA (NOVO) ---
        // Verifica se veio idioma no DTO. Se for nulo ou vazio, usa "pt".
        String idiomaDefinido = (dados.idioma() != null && !dados.idioma().isBlank())
                ? dados.idioma()
                : "pt";

        // --- CRIAÇÃO DO USUÁRIO COM IDIOMA ---
        Usuario novoUsuario = new Usuario(
                dados.nome(),
                dados.email(),
                senhaHash,
                tipoUsuarioEnum,
                idiomaDefinido // Passando o idioma para o novo construtor
        );

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

        logger.info("Registro do usuário {} concluído com sucesso.", dados.email());
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
            logger.error("Erro ao criar vitrine no MongoDB para usuário: " + usuario.getIdUsuario(), e);
        }
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));
    }

    public List<Usuario> buscarPorTipo(String tipoUsuario) {
        TipoUsuario tipo = TipoUsuario.valueOf(tipoUsuario.toUpperCase());
        return usuarioRepository.findByTipoUsuario(tipo);
    }
}