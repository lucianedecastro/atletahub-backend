package br.com.atletahub.atletahub_backend.dto.interesse;

import br.com.atletahub.atletahub_backend.enums.TipoInteresse;
import jakarta.validation.constraints.NotNull; // Importar NotNull

public record DadosCadastroInteresse(
        @NotNull // Garante que o ID do destino não seja nulo
        Long idDestino, // ID do usuário que receberá o interesse

        @NotNull // Garante que o tipo de interesse não seja nulo
        TipoInteresse tipoInteresse // O tipo de interesse (CURTIR, SUPER_CURTIR)
) {
}
