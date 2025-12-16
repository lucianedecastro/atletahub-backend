package br.com.atletahub.atletahub_backend.service;

import com.cloudinary.Cloudinary;
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
}
