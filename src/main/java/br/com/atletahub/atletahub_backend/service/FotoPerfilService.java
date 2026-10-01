package br.com.atletahub.atletahub_backend.service;

import br.com.atletahub.atletahub_backend.model.PerfilAtleta;
import br.com.atletahub.atletahub_backend.model.PerfilMarca;
import br.com.atletahub.atletahub_backend.model.TipoUsuario;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.repository.PerfilAtletaRepository;
import br.com.atletahub.atletahub_backend.repository.PerfilMarcaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Set;

/**
 * Foto de perfil do atleta e logo da marca. Salva a URL direto no perfil (sem depender de o usuário
 * clicar em "Salvar") e não mexe na vitrine de fotos.
 */
@Service
public class FotoPerfilService {

    private static final Logger logger = LoggerFactory.getLogger(FotoPerfilService.class);

    private static final long LIMITE_BYTES = 5L * 1024 * 1024; // 5 MB (o front já reduz antes de enviar)

    // SVG fica de fora de propósito: pode carregar script.
    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif", "image/heic", "image/heif");

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private PerfilAtletaRepository perfilAtletaRepository;

    @Autowired
    private PerfilMarcaRepository perfilMarcaRepository;

    /** Envia a imagem e grava a URL no perfil do usuário logado. Retorna a nova URL. */
    public String atualizar(Usuario usuario, MultipartFile arquivo) {
        validar(arquivo);
        TipoUsuario tipo = usuario.getTipoUsuario();
        if (tipo != TipoUsuario.ATLETA && tipo != TipoUsuario.MARCA) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este tipo de conta não tem foto de perfil.");
        }

        String url;
        try {
            url = cloudinaryService.uploadImagemPerfil(arquivo, tipo == TipoUsuario.ATLETA);
        } catch (IOException | RuntimeException e) {
            logger.error("Falha ao enviar foto de perfil (usuário {})", usuario.getIdUsuario(), e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Não foi possível enviar a imagem agora. Tente novamente em instantes.");
        }
        if (url == null || url.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Não foi possível enviar a imagem agora. Tente novamente em instantes.");
        }

        gravar(usuario, url);
        return url;
    }

    /** Remove a foto (volta para as iniciais no front). */
    public void remover(Usuario usuario) {
        gravar(usuario, null);
    }

    private void gravar(Usuario usuario, String url) {
        Long id = usuario.getIdUsuario();
        if (usuario.getTipoUsuario() == TipoUsuario.ATLETA) {
            PerfilAtleta perfil = perfilAtletaRepository.findByUsuarioId(id)
                    .orElseGet(() -> new PerfilAtleta(id));
            perfil.definirFoto(url);
            perfilAtletaRepository.save(perfil);
        } else if (usuario.getTipoUsuario() == TipoUsuario.MARCA) {
            PerfilMarca perfil = perfilMarcaRepository.findByUsuarioId(id)
                    .orElseGet(() -> new PerfilMarca(id));
            perfil.setLogoUrl(url == null ? "" : url);
            perfilMarcaRepository.save(perfil);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este tipo de conta não tem foto de perfil.");
        }
    }

    private void validar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma imagem para enviar.");
        }
        String tipo = arquivo.getContentType() == null ? "" : arquivo.getContentType().toLowerCase();
        if (!TIPOS_PERMITIDOS.contains(tipo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Formato não aceito. Envie uma foto JPG, PNG ou WEBP.");
        }
        if (arquivo.getSize() > LIMITE_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "A imagem deve ter no máximo 5 MB.");
        }
    }
}
