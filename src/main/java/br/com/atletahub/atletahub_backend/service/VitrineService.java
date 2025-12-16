package br.com.atletahub.atletahub_backend.service;

import br.com.atletahub.atletahub_backend.model.mongo.PerfilVitrine;
import br.com.atletahub.atletahub_backend.repository.PerfilVitrineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;

@Service
public class VitrineService {

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

    public PerfilVitrine adicionarMidia(Long usuarioId, MultipartFile arquivo, String tipo) throws IOException {
        // 1. Faz o upload para a nuvem
        String url = cloudinaryService.uploadArquivo(arquivo);

        // 2. Busca o documento no Mongo (ou cria)
        PerfilVitrine vitrine = buscarPorUsuarioId(usuarioId);

        // 3. Adiciona a URL na lista correta
        if ("VIDEO".equalsIgnoreCase(tipo)) {
            if (vitrine.getVideos() == null) vitrine.setVideos(new ArrayList<>());
            vitrine.getVideos().add(url);
        } else {
            // Assume FOTO como padrão
            if (vitrine.getFotos() == null) vitrine.setFotos(new ArrayList<>());
            vitrine.getFotos().add(url);
        }

        // 4. Salva a atualização no Mongo
        return perfilVitrineRepository.save(vitrine);
    }
}
