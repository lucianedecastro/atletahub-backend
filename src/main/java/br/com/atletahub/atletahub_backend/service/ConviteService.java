package br.com.atletahub.atletahub_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Controle de entrada no beta fechado: o cadastro só é aceito com um código de convite.
 *
 * Os códigos ficam na variável de ambiente CONVITE_CODIGOS (no Render), separados por vírgula,
 * por exemplo: CONVITE_CODIGOS=BETA-ATLETAS-7K2QX9,BETA-MARCAS-4M8WZ3
 * - Variável vazia ou ausente: o cadastro fica aberto (sem código).
 * - O código não diferencia maiúsculas de minúsculas e ignora espaços nas pontas.
 * - O código nunca vai para o log.
 */
@Service
public class ConviteService {

    private final List<byte[]> codigos = new ArrayList<>();

    public ConviteService(@Value("${CONVITE_CODIGOS:}") String bruto) {
        if (bruto == null) {
            return;
        }
        for (String parte : bruto.split(",")) {
            String normalizado = normalizar(parte);
            if (!normalizado.isEmpty()) {
                codigos.add(normalizado.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    /** True quando há ao menos um código configurado (cadastro fechado, só por convite). */
    public boolean exigido() {
        return !codigos.isEmpty();
    }

    /** Sem código configurado, qualquer cadastro passa. Com código, só passa quem digitou um válido. */
    public boolean valido(String informado) {
        if (!exigido()) {
            return true;
        }
        byte[] tentativa = normalizar(informado).getBytes(StandardCharsets.UTF_8);
        boolean achou = false;
        // Percorre todos, sem parar no primeiro, e compara em tempo constante.
        for (byte[] codigo : codigos) {
            if (MessageDigest.isEqual(codigo, tentativa)) {
                achou = true;
            }
        }
        return achou;
    }

    private static String normalizar(String texto) {
        return texto == null ? "" : texto.trim().toUpperCase(Locale.ROOT);
    }
}
