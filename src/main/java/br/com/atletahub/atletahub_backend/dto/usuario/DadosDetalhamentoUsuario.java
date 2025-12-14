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
        BigDecimal peso
) {

    // ALTERAÇÃO: Agora o construtor recebe os objetos separadamente.
    // Quem chamar esse DTO (no Controller) terá que buscar os perfis antes.
    public DadosDetalhamentoUsuario(Usuario usuario, PerfilAtleta perfilAtleta, PerfilMarca perfilMarca) {
        this(
                usuario.getIdUsuario(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTipoUsuario().toString(),

                // Mapeia dados de ATLETA se o objeto não for nulo
                perfilAtleta != null ? perfilAtleta.getIdade() : null,
                perfilAtleta != null ? perfilAtleta.getModalidade() : null,
                perfilAtleta != null ? perfilAtleta.getCompeticoesTitulos() : null,
                perfilAtleta != null ? perfilAtleta.getRedesSocial() : (perfilMarca != null ? perfilMarca.getRedesSocial() : null), // Redes sociais pode vir de ambos
                perfilAtleta != null ? perfilAtleta.getHistorico() : null,

                // Mapeia dados de MARCA se o objeto não for nulo
                perfilMarca != null ? perfilMarca.getProduto() : null,
                perfilMarca != null ? perfilMarca.getTempoMercado() : null,
                perfilMarca != null ? perfilMarca.getAtletasPatrocinados() : null,
                perfilMarca != null ? perfilMarca.getTipoInvestimento() : null,

                // Demais dados de atleta
                perfilAtleta != null ? perfilAtleta.getAltura() : null,
                perfilAtleta != null ? perfilAtleta.getPeso() : null
        );
    }
}