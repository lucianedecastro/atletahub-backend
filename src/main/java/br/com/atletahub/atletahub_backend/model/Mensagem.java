package br.com.atletahub.atletahub_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table(name = "mensagem")
@Entity(name = "Mensagem")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensagem")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_match", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_remetente", nullable = false)
    private Usuario remetente;

    @Column(name = "texto", columnDefinition = "TEXT", nullable = false)
    private String texto;

    @Column(name = "data_envio", nullable = false)
    private LocalDateTime dataEnvio;


    public Mensagem(Match match, Usuario remetente, String texto) {
        this.match = match;
        this.remetente = remetente;
        this.texto = texto;
        this.dataEnvio = LocalDateTime.now();
    }
}
