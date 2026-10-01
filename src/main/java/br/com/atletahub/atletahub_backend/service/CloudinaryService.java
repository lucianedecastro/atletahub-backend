package br.com.atletahub.atletahub_backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadArquivo(MultipartFile file) throws IOException {
        // "resource_type: auto" permite upload de imagens E vídeos
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "resource_type", "auto"
        ));

        // Retorna a URL segura (https) para salvarmos no banco
        return (String) uploadResult.get("secure_url");
    }

    /**
     * Foto de perfil / logo. A transformação é aplicada no upload (o Cloudinary guarda só a versão
     * já reduzida, em vez dos 10 MB originais), então os cards do Dashboard carregam rápido.
     *
     * @param quadrado true = atleta (recorta em quadrado 600x600 focando no rosto/assunto);
     *                 false = marca (só reduz, SEM cortar, para não estragar a logo).
     */
    public String uploadImagemPerfil(MultipartFile file, boolean quadrado) throws IOException {
        Transformation transformacao = quadrado
                ? new Transformation().width(600).height(600).crop("fill").gravity("auto")
                        .quality("auto").fetchFormat("auto")
                : new Transformation().width(600).height(600).crop("limit")
                        .quality("auto").fetchFormat("auto");

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "resource_type", "image",
                "folder", "atletahub/perfil",
                "transformation", transformacao
        ));

        return (String) uploadResult.get("secure_url");
    }
}
