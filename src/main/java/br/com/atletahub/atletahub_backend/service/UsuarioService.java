package br.com.atletahub.atletahub_backend.service;

import br.com.atletahub.atletahub_backend.dto.usuario.DadosRegistroUsuario;
import br.com.atletahub.atletahub_backend.model.TipoUsuario;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.repository.PerfilAtletaRepository;
import br.com.atletahub.atletahub_backend.repository.PerfilMarcaRepository;
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
import java.util.Optional;

@Service
public class UsuarioService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final PerfilAtletaService perfilAtletaService;
    private final PerfilMarcaService perfilMarcaService;
    private final PerfilAtletaRepository perfilAtletaRepository;
    private final PerfilMarcaRepository perfilMarcaRepository;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          PerfilAtletaService perfilAtletaService,
                          PerfilMarcaService perfilMarcaService,
                          PerfilAtletaRepository perfilAtletaRepository,
                          PerfilMarcaRepository perfilMarcaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.perfilAtletaService = perfilAtletaService;
        this.perfilMarcaService = perfilMarcaService;
        this.perfilAtletaRepository = perfilAtletaRepository;
        this.perfilMarcaRepository = perfilMarcaRepository;
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
        Usuario novoUsuario = new Usuario(dados.nome(), dados.email(), senhaHash, tipoUsuarioEnum);


        Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);
        logger.info("Usuário salvo com ID: {}", usuarioSalvo.getIdUsuario());

        if (usuarioSalvo.getTipoUsuario() == TipoUsuario.ATLETA) {
            perfilAtletaService.criarPerfilAtletaInicial(usuarioSalvo);
        } else if (usuarioSalvo.getTipoUsuario() == TipoUsuario.MARCA) {
            perfilMarcaService.criarPerfilMarcaInicial(usuarioSalvo);
        } else if (usuarioSalvo.getTipoUsuario() == TipoUsuario.ADMIN) {

        }

        logger.info("Registro do usuário {} concluído com sucesso.", dados.email());
        return usuarioSalvo;
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