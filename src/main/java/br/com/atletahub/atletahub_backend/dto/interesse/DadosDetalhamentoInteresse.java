package br.com.atletahub.atletahub_backend.dto.interesse;

import br.com.atletahub.atletahub_backend.enums.TipoInteresse;
import br.com.atletahub.atletahub_backend.model.Interesse;
import br.com.atletahub.atletahub_backend.model.Usuario;

import java.time.LocalDateTime;

public record DadosDetalhamentoInteresse(
        Long id,
        Long idOrigem,
        Long idDestino,
        TipoInteresse tipoInteresse,
        LocalDateTime dataEnvio
) {

    public DadosDetalhamentoInteresse(Interesse interesse) {
        this(
                interesse.getId(),

                interesse.getOrigem().getIdUsuario(),
                interesse.getDestino().getIdUsuario(),
                interesse.getTipoInteresse(),
                interesse.getDataEnvio()
        );
    }
}