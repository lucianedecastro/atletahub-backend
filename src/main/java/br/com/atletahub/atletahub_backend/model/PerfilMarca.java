package br.com.atletahub.atletahub_backend.model;

import br.com.atletahub.atletahub_backend.dto.perfil.DadosAtualizacaoPerfilMarca;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "perfil_marca")
@Entity(name = "PerfilMarca")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id_perfil_marca")
public class PerfilMarca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_perfil_marca;

    // ALTERAÇÃO: Apenas o ID
    @Column(name = "id_usuario", unique = true, nullable = false)
    private Long usuarioId;

    private String produto;

    @Column(name = "tempo_mercado")
    private Integer tempoMercado;

    @Column(name = "atletas_patrocinados", columnDefinition = "TEXT")
    private String atletasPatrocinados;

    @Column(name = "tipo_investimento", length = 255)
    private String tipoInvestimento;

    @Column(name = "redes_social")
    private String redesSocial;

    public PerfilMarca(Long usuarioId) {
        this.usuarioId = usuarioId;
        this.produto = "";
        this.tempoMercado = 0;
        this.atletasPatrocinados = "";
        this.tipoInvestimento = "";
        this.redesSocial = "";
    }

    public void atualizar(DadosAtualizacaoPerfilMarca dados) {
        if (dados.produto() != null) this.produto = dados.produto();
        if (dados.tempoMercado() != null) this.tempoMercado = dados.tempoMercado();
        if (dados.atletasPatrocinados() != null) this.atletasPatrocinados = dados.atletasPatrocinados();
        if (dados.tipoInvestimento() != null) this.tipoInvestimento = dados.tipoInvestimento();
        if (dados.redesSocial() != null) this.redesSocial = dados.redesSocial();
    }
}