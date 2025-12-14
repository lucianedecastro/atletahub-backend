package br.com.atletahub.atletahub_backend.model.mongo;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Midia {
    private String url;       // URL do Cloudinary
    private String publicId;  // ID no Cloudinary (para poder deletar depois)
    private String tipo;      // "FOTO" ou "VIDEO"
    private String titulo;
}