package br.com.atletahub.atletahub_backend.dto.perfil;

import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DadosAtualizacaoPerfilAtleta(
        String nome,   // <--- NOVO
        String email,  // <--- NOVO
        Integer idade,
        @Size(max = 255)
        String modalidade,
        @Size(max = 255)
        String posicao,
        BigDecimal altura,
        BigDecimal peso,
        LocalDate dataNascimento,
        @Size(max = 255)
        String telefoneContato,
        @Size(max = 255)
        String observacoes,
        @Size(max = 255)
        String midiakitUrl,
        String competicoesTitulos,
        String redesSocial,
        String historico
) {}