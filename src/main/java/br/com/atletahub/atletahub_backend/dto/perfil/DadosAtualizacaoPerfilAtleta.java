package br.com.atletahub.atletahub_backend.dto.perfil;

import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record DadosAtualizacaoPerfilAtleta(
        String nome,
        String email,
        Integer idade,
        Double altura,
        Double peso,
        String modalidade,
        String competicoesTitulos,
        String redesSocial,
        String historico,
        String posicao,
        String observacoes,
        LocalDate dataNascimento,
        String telefoneContato,
        String midiakitUrl
) {
}