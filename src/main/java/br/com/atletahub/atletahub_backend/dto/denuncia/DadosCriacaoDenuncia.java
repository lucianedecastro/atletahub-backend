package br.com.atletahub.atletahub_backend.dto.denuncia;

import br.com.atletahub.atletahub_backend.enums.MotivoDenuncia;
import br.com.atletahub.atletahub_backend.enums.TipoAlvoDenuncia;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DadosCriacaoDenuncia(

        @NotNull(message = "Informe quem está sendo denunciado")
        Long idDenunciado,

        @NotNull(message = "Informe o que está sendo denunciado")
        TipoAlvoDenuncia tipoAlvo,

        @NotNull(message = "Escolha o motivo da denúncia")
        MotivoDenuncia motivo,

        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
        String descricao,

        // PERFIL: vazio | MIDIA: endereço da foto ou do vídeo | MENSAGEM: número da mensagem
        @Size(max = 500, message = "Referência inválida")
        String referencia
) {
}
