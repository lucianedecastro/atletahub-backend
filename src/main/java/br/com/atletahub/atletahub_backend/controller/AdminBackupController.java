package br.com.atletahub.atletahub_backend.controller;

import br.com.atletahub.atletahub_backend.model.mongo.PerfilVitrine;
import br.com.atletahub.atletahub_backend.repository.PerfilVitrineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cópia de segurança das vitrines (Mongo), para o admin baixar pelo painel.
 * Como as demais rotas /admin, exige a X-Admin-Key (AdminAccessFilter) e o papel ADMIN (SecurityConfig).
 * O Postgres tem backup próprio no Neon; aqui é só a coleção perfil_vitrine.
 */
@RestController
@RequestMapping("/admin/backup")
public class AdminBackupController {

    private static final Logger logger = LoggerFactory.getLogger(AdminBackupController.class);

    @Autowired
    private PerfilVitrineRepository perfilVitrineRepository;

    @GetMapping("/vitrines")
    public ResponseEntity<Map<String, Object>> vitrines() {
        List<Map<String, Object>> itens = new ArrayList<>();
        for (PerfilVitrine v : perfilVitrineRepository.findAll()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", v.getId());
            item.put("usuarioId", v.getUsuarioId());
            item.put("biografiaCompleta", v.getBiografiaCompleta());
            item.put("fotos", v.getFotos() == null ? List.of() : v.getFotos());
            item.put("videos", v.getVideos() == null ? List.of() : v.getVideos());
            itens.add(item);
        }

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("colecao", "perfil_vitrine");
        corpo.put("geradoEm", Instant.now().toString());
        corpo.put("total", itens.size());
        corpo.put("documentos", itens);

        logger.info("Backup das vitrines gerado pelo painel admin ({} documento(s))", itens.size());
        return ResponseEntity.ok()
                .header("Cache-Control", "no-store")
                .body(corpo);
    }
}
