package br.com.atletahub.atletahub_backend.dto.usuario;

import br.com.atletahub.atletahub_backend.model.PerfilAtleta;
import br.com.atletahub.atletahub_backend.model.PerfilMarca;
import br.com.atletahub.atletahub_backend.model.Usuario;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DadosDetalhamentoUsuario(
        Long id,
        String nome,
        String email,
        String tipoUsuario,

        Integer idade,
        String modalidade,
        String competicoesTitulos,
        String redesSocial,
        String historico,

        String produto,
        Integer tempoMercado,
        String atletasPatrocinados,
        String tipoInvestimento
) {
    public DadosDetalhamentoUsuario(Usuario usuario) {
        this(
                usuario.getIdUsuario(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTipoUsuario().toString(),
                Optional.ofNullable(usuario.getPerfilAtleta()).map(PerfilAtleta::getIdade).orElse(null),
                Optional.ofNullable(usuario.getPerfilAtleta()).map(PerfilAtleta::getModalidade).orElse(null),
                Optional.ofNullable(usuario.getPerfilAtleta()).map(PerfilAtleta::getCompeticoesTitulos).orElse(null),
                Optional.ofNullable(usuario.getPerfilAtleta()).map(PerfilAtleta::getRedesSocial).orElse(null),
                Optional.ofNullable(usuario.getPerfilAtleta()).map(PerfilAtleta::getHistorico).orElse(null),
                Optional.ofNullable(usuario.getPerfilMarca()).map(PerfilMarca::getProduto).orElse(null),
                Optional.ofNullable(usuario.getPerfilMarca()).map(PerfilMarca::getTempoMercado).orElse(null),
                Optional.ofNullable(usuario.getPerfilMarca()).map(PerfilMarca::getAtletasPatrocinados).orElse(null),
                Optional.ofNullable(usuario.getPerfilMarca()).map(PerfilMarca::getTipoInvestimento).orElse(null)
        );
    }
}