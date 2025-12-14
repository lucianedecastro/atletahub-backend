package br.com.atletahub.atletahub_backend.dto.perfil;

import br.com.atletahub.atletahub_backend.model.PerfilAtleta;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DadosDetalhamentoPerfilAtleta(
        Long idPerfilAtleta,
        Long idUsuario,
        Integer idade,
        BigDecimal altura,
        BigDecimal peso,
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
    public DadosDetalhamentoPerfilAtleta(PerfilAtleta perfil) {
        this(
                perfil.getIdPerfilAtleta(),
                perfil.getUsuarioId(), // Pega o ID direto
                perfil.getIdade(),
                perfil.getAltura(),
                perfil.getPeso(),
                perfil.getModalidade(),
                perfil.getCompeticoesTitulos(),
                perfil.getRedesSocial(),
                perfil.getHistorico(),
                perfil.getPosicao(),
                perfil.getObservacoes(),
                perfil.getDataNascimento(),
                perfil.getTelefoneContato(),
                perfil.getMidiakitUrl()
        );
    }
}
