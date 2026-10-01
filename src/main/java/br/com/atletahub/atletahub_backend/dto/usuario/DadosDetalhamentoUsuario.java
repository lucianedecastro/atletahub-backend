package br.com.atletahub.atletahub_backend.dto.usuario;

import br.com.atletahub.atletahub_backend.model.PerfilAtleta;
import br.com.atletahub.atletahub_backend.model.PerfilMarca;
import br.com.atletahub.atletahub_backend.model.Usuario;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

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
        String tipoInvestimento,
        BigDecimal altura,
        BigDecimal peso,
        String cidade,
        String estado,
        String posicao,
        String midiakitUrl,
        String logoUrl
) {

    /**
     * Monta o DTO público/privado.
     * O e-mail só vai no JSON quando {@code incluirEmail} é true (o próprio usuário ou um ADMIN).
     * Para os demais usuários o campo fica null e, por causa do @JsonInclude, nem aparece.
     */
    public DadosDetalhamentoUsuario(Usuario usuario, PerfilAtleta perfilAtleta, PerfilMarca perfilMarca, boolean incluirEmail) {
        this(
                usuario.getIdUsuario(),
                usuario.getNome(),
                incluirEmail ? usuario.getEmail() : null,
                usuario.getTipoUsuario().toString(),

                // Mapeia dados de ATLETA se o objeto não for nulo
                perfilAtleta != null ? perfilAtleta.getIdade() : null,
                perfilAtleta != null ? perfilAtleta.getModalidade() : null,
                perfilAtleta != null ? perfilAtleta.getCompeticoesTitulos() : null,
                perfilAtleta != null ? perfilAtleta.getRedesSocial() : (perfilMarca != null ? perfilMarca.getRedesSocial() : null),
                perfilAtleta != null ? perfilAtleta.getHistorico() : null,

                // Mapeia dados de MARCA se o objeto não for nulo
                perfilMarca != null ? perfilMarca.getProduto() : null,
                perfilMarca != null ? perfilMarca.getTempoMercado() : null,
                perfilMarca != null ? perfilMarca.getAtletasPatrocinados() : null,
                perfilMarca != null ? perfilMarca.getTipoInvestimento() : null,

                // Demais dados de atleta
                perfilAtleta != null ? perfilAtleta.getAltura() : null,
                perfilAtleta != null ? perfilAtleta.getPeso() : null,

                usuario.getCidade(),
                usuario.getEstado(),

                // Dados públicos que a tela de perfil de outro usuário precisa exibir
                perfilAtleta != null ? perfilAtleta.getPosicao() : null,
                perfilAtleta != null ? perfilAtleta.getMidiakitUrl() : null,
                perfilMarca != null ? perfilMarca.getLogoUrl() : null
        );
    }

    // Mantido por compatibilidade: SEM e-mail (padrão seguro).
    public DadosDetalhamentoUsuario(Usuario usuario, PerfilAtleta perfilAtleta, PerfilMarca perfilMarca) {
        this(usuario, perfilAtleta, perfilMarca, false);
    }
}
