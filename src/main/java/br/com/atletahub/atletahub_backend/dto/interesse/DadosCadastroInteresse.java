package br.com.atletahub.atletahub_backend.dto.interesse;

import br.com.atletahub.atletahub_backend.enums.TipoInteresse;
import jakarta.validation.constraints.NotNull;

public record DadosCadastroInteresse(
        @NotNull
        Long idDestino,

        @NotNull
        TipoInteresse tipoInteresse
) {
}
