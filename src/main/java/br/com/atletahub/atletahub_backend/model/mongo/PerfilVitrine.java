package br.com.atletahub.atletahub_backend.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "perfil_vitrine")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerfilVitrine {

    @Id
    private String id;

    private Long usuarioId;

    private String biografiaCompleta;

    // --- USAMOS STRINGS DIRETAS PARA URLS ---
    // Isso garante compatibilidade imediata com o VitrineService e o Frontend
    private List<String> fotos = new ArrayList<>();

    private List<String> videos = new ArrayList<>();

    // Construtor auxiliar
    public PerfilVitrine(Long usuarioId) {
        this.usuarioId = usuarioId;
        this.fotos = new ArrayList<>();
        this.videos = new ArrayList<>();
    }
}