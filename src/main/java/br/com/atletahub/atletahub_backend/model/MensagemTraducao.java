package br.com.atletahub.atletahub_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table(name = "mensagem_traducao")
@Entity(name = "MensagemTraducao")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class MensagemTraducao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_traducao")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mensagem", nullable = false)
    private Mensagem mensagem;

    @Column(name = "idioma_origem", length = 10, nullable = false)
    private String idiomaOrigem;

    @Column(name = "idioma_destino", length = 10, nullable = false)
    private String idiomaDestino;

    @Column(name = "texto_traduzido", columnDefinition = "TEXT", nullable = false)
    private String textoTraduzido;

    @Column(name = "data_traducao", nullable = false)
    private LocalDateTime dataTraducao;

    public MensagemTraducao(
            Mensagem mensagem,
            String idiomaOrigem,
            String idiomaDestino,
            String textoTraduzido
    ) {
        this.mensagem = mensagem;
        this.idiomaOrigem = idiomaOrigem;
        this.idiomaDestino = idiomaDestino;
        this.textoTraduzido = textoTraduzido;
        this.dataTraducao = LocalDateTime.now();
    }
}

