package br.com.atletahub.atletahub_backend.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Uma pessoa (bloqueador) bloqueou outra (bloqueado). */
@Table(name = "bloqueio")
@Entity(name = "Bloqueio")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Bloqueio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bloqueio")
    private Long id;

    @Column(name = "id_bloqueador", nullable = false)
    private Long idBloqueador;

    @Column(name = "id_bloqueado", nullable = false)
    private Long idBloqueado;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();

    public Bloqueio(Long idBloqueador, Long idBloqueado) {
        this.idBloqueador = idBloqueador;
        this.idBloqueado = idBloqueado;
    }
}
