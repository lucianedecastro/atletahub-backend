package br.com.atletahub.atletahub_backend.model.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "vitrines")
public class PerfilVitrine {

    @Id
    private String id; // ID interno do Mongo

    @Indexed(unique = true)
    private Long usuarioId; // Link com o Postgres

    private String biografiaCompleta;
    private List<Midia> galeria = new ArrayList<>();

    // Construtor auxiliar
    public PerfilVitrine(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
}