package br.com.atletahub.atletahub_backend.model;

import br.com.atletahub.atletahub_backend.dto.perfil.DadosAtualizacaoPerfilAtleta;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Table(name = "perfil_atleta")
@Entity(name = "PerfilAtleta")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idPerfilAtleta")
public class PerfilAtleta {

    // Abaixo deste valor a altura é tratada como METROS (ex.: 1.75) e convertida para cm.
    private static final BigDecimal LIMITE_METROS = new BigDecimal("3.00");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perfil_atleta")
    private Long idPerfilAtleta;

    // ALTERAÇÃO: Armazenamos apenas o ID para desacoplar e facilitar a migração
    @Column(name = "id_usuario", unique = true, nullable = false)
    private Long usuarioId;

    private Integer idade;

    // Altura em CENTÍMETROS (ex.: 175.00). A coluna foi ampliada para NUMERIC(5,2) na migração V5
    // (antes era DECIMAL(3,2), que só aceitava até 9.99 e quebrava com valores em cm).
    @Column(precision = 5, scale = 2)
    private BigDecimal altura;

    @Column(precision = 5, scale = 2)
    private BigDecimal peso;

    private String modalidade;

    @Column(name = "competicoes_titulos", columnDefinition = "TEXT")
    private String competicoesTitulos;

    @Column(name = "redes_social")
    private String redesSocial;

    @Column(columnDefinition = "TEXT")
    private String historico;

    private String posicao;
    private String observacoes;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "telefone_contato")
    private String telefoneContato;

    @Column(name = "midiakit_url")
    private String midiakitUrl;

    // Construtor completo ajustado para receber Long usuarioId
    public PerfilAtleta(Long usuarioId, Integer idade, BigDecimal altura, BigDecimal peso, String modalidade, String competicoesTitulos, String redesSocial, String historico, String posicao, String observacoes, LocalDate dataNascimento, String telefoneContato, String midiakitUrl) {
        this.usuarioId = usuarioId;
        this.idade = idade;
        this.altura = altura;
        this.peso = peso;
        this.modalidade = modalidade;
        this.competicoesTitulos = competicoesTitulos;
        this.redesSocial = redesSocial;
        this.historico = historico;
        this.posicao = posicao;
        this.observacoes = observacoes;
        this.dataNascimento = dataNascimento;
        this.telefoneContato = telefoneContato;
        this.midiakitUrl = midiakitUrl;
    }

    // Construtor inicial (apenas ID)
    public PerfilAtleta(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    /**
     * Regras de atualização:
     * - campo ausente (null) = não altera;
     * - texto vazio ("") = LIMPA o campo (antes era ignorado e o atleta não conseguia apagar nada);
     * - modalidade não pode ficar vazia (é a base do match), então vazio é ignorado.
     */
    public void atualizarInformacoes(DadosAtualizacaoPerfilAtleta dados) {
        if (dados.idade() != null) this.idade = dados.idade();
        if (dados.altura() != null) this.altura = normalizarAltura(dados.altura());
        if (dados.peso() != null) this.peso = dados.peso();
        if (dados.modalidade() != null && !dados.modalidade().isBlank()) this.modalidade = dados.modalidade().trim();
        if (dados.competicoesTitulos() != null) this.competicoesTitulos = dados.competicoesTitulos();
        if (dados.redesSocial() != null) this.redesSocial = dados.redesSocial();
        if (dados.historico() != null) this.historico = dados.historico();
        if (dados.posicao() != null) this.posicao = dados.posicao();
        if (dados.observacoes() != null) this.observacoes = dados.observacoes();
        if (dados.dataNascimento() != null) this.dataNascimento = dados.dataNascimento();
        if (dados.telefoneContato() != null) this.telefoneContato = dados.telefoneContato();
        if (dados.midiakitUrl() != null) this.midiakitUrl = dados.midiakitUrl();
    }

    public void atualizar(DadosAtualizacaoPerfilAtleta dados) {
        this.atualizarInformacoes(dados);
    }

    private static BigDecimal normalizarAltura(BigDecimal valor) {
        if (valor.compareTo(LIMITE_METROS) < 0) {
            return valor.multiply(new BigDecimal("100")).setScale(2, java.math.RoundingMode.HALF_UP);
        }
        return valor.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
