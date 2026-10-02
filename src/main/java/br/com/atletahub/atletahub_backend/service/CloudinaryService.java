package br.com.atletahub.atletahub_backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CloudinaryService {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryService.class);

    // Endereço de um arquivo nosso no Cloudinary: .../<cloud>/<image|video|raw>/upload/[v123/]<id>[.extensão]
    private static final Pattern ENDERECO_NA_NUVEM =
            Pattern.compile("^https://res\\.cloudinary\\.com/([^/]+)/(image|video|raw)/upload/(?:v\\d+/)?([^?#]+)");

    @Autowired
    private Cloudinary cloudinary;

    @Value("${cloudinary.cloud_name}")
    private String cloudName;

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

    /**
     * Apaga no Cloudinary os arquivos cujos endereços foram informados.
     * Só mexe em arquivos da NOSSA conta do Cloudinary: endereços de outros sites, ou vazios, são ignorados.
     * Falha em um arquivo não impede os demais (fica no log, sem dado pessoal).
     * Devolve quantos foram apagados.
     */
    public int apagarPorUrls(Collection<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return 0;
        }

        int apagados = 0;
        int ignorados = 0;
        int falhas = 0;
        Set<String> jaVistos = new HashSet<>();

        for (String url : urls) {
            if (url == null || url.isBlank()) {
                continue;
            }
            Matcher m = ENDERECO_NA_NUVEM.matcher(url.trim());
            if (!m.find() || !m.group(1).equals(cloudName)) {
                ignorados++;
                continue;
            }

            String tipo = m.group(2);
            String publicId = m.group(3);
            // Imagem e vídeo: o id não leva a extensão. Arquivo "raw": o id é o nome com extensão.
            if (!"raw".equals(tipo)) {
                publicId = publicId.replaceFirst("\\.[A-Za-z0-9]{2,5}$", "");
            }
            if (!jaVistos.add(tipo + "/" + publicId)) {
                continue;
            }

            try {
                Map resposta = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap(
                        "resource_type", tipo,
                        "invalidate", true));
                Object resultado = resposta == null ? null : resposta.get("result");
                if ("ok".equals(resultado)) {
                    apagados++;
                } else {
                    falhas++;
                    logger.warn("Cloudinary não apagou o arquivo (tipo {}, id {}): {}", tipo, publicId, resultado);
                }
            } catch (Exception e) {
                falhas++;
                logger.warn("Falha ao apagar arquivo no Cloudinary (tipo {}, id {}): {}",
                        tipo, publicId, e.getClass().getSimpleName());
            }
        }

        logger.info("Cloudinary: {} arquivo(s) apagado(s), {} falha(s), {} endereço(s) ignorado(s)",
                apagados, falhas, ignorados);
        return apagados;
    }
}
