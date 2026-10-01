package br.com.atletahub.atletahub_backend.service;

import br.com.atletahub.atletahub_backend.model.mongo.PerfilVitrine;
import br.com.atletahub.atletahub_backend.repository.PerfilVitrineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;

@Service
public class VitrineService {

    private static final Logger logger = LoggerFactory.getLogger(VitrineService.class);

    // Limites por tipo (o servidor do Render free tem pouca memória: o arquivo passa pela heap no upload).
    private static final long LIMITE_FOTO_BYTES = 10L * 1024 * 1024;   // 10 MB
    private static final long LIMITE_VIDEO_BYTES = 50L * 1024 * 1024;  // 50 MB

    @Autowired
    private PerfilVitrineRepository perfilVitrineRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    public PerfilVitrine buscarPorUsuarioId(Long usuarioId) {
        return perfilVitrineRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> {
                    // Se não existir vitrine ainda, cria uma vazia na hora
                    PerfilVitrine nova = new PerfilVitrine(usuarioId);
                    return perfilVitrineRepository.save(nova);
                });
    }

    public PerfilVitrine adicionarMidia(Long usuarioId, MultipartFile arquivo, String tipo) {
        // 0. Valida ANTES de gastar upload na nuvem
        boolean video = validarArquivo(arquivo, tipo);

        // 1. Faz o upload para a nuvem
        String url;
        try {
            url = cloudinaryService.uploadArquivo(arquivo);
        } catch (IOException | RuntimeException e) {
            logger.error("Falha no upload para o Cloudinary (usuário {})", usuarioId, e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Não foi possível enviar o arquivo agora. Tente novamente em instantes.");
        }

        if (url == null || url.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Não foi possível enviar o arquivo agora. Tente novamente em instantes.");
        }

        // 2. Busca o documento no Mongo (ou cria)
        PerfilVitrine vitrine = buscarPorUsuarioId(usuarioId);

        // 3. Adiciona a URL na lista correta
        if (video) {
            if (vitrine.getVideos() == null) vitrine.setVideos(new ArrayList<>());
            vitrine.getVideos().add(url);
        } else {
            if (vitrine.getFotos() == null) vitrine.setFotos(new ArrayList<>());
            vitrine.getFotos().add(url);
        }

        // 4. Salva a atualização no Mongo
        return perfilVitrineRepository.save(vitrine);
    }

    /** Retorna true se for vídeo, false se for foto. Lança 400/413 se o arquivo não servir. */
    private boolean validarArquivo(MultipartFile arquivo, String tipo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione um arquivo para enviar.");
        }

        boolean video;
        if ("VIDEO".equalsIgnoreCase(tipo)) {
            video = true;
        } else if ("FOTO".equalsIgnoreCase(tipo)) {
            video = false;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de mídia inválido. Use FOTO ou VIDEO.");
        }

        String contentType = arquivo.getContentType() != null ? arquivo.getContentType().toLowerCase() : "";
        if (video && !contentType.startsWith("video/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O arquivo enviado não é um vídeo.");
        }
        if (!video && !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O arquivo enviado não é uma imagem.");
        }

        long limite = video ? LIMITE_VIDEO_BYTES : LIMITE_FOTO_BYTES;
        if (arquivo.getSize() > limite) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                    video ? "O vídeo deve ter no máximo 50 MB." : "A foto deve ter no máximo 10 MB.");
        }

        return video;
    }
}
