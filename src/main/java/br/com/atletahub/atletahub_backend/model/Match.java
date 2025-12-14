package br.com.atletahub.atletahub_backend.model;

import br.com.atletahub.atletahub_backend.enums.TipoMatch;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Table(name = "match_table", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_usuario_a", "id_usuario_b"})
})
@Entity(name = "Match")
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_match")
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_a", referencedColumnName = "id_usuario", nullable = false)
    private Usuario usuarioA;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_b", referencedColumnName = "id_usuario", nullable = false)
    private Usuario usuarioB;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_match", nullable = false)
    private TipoMatch tipoMatch;

    @Column(name = "data_match", nullable = false)
    private LocalDateTime dataMatch;


    public Match(Usuario usuario1, Usuario usuario2, TipoMatch tipoMatch) {


        if (usuario1.getIdUsuario() < usuario2.getIdUsuario()) {
            this.usuarioA = usuario1;
            this.usuarioB = usuario2;
        } else {
            this.usuarioA = usuario2;
            this.usuarioB = usuario1;
        }
        this.tipoMatch = tipoMatch;
        this.dataMatch = LocalDateTime.now();
    }

    public Long getIdUsuarioA() {
        return this.usuarioA.getIdUsuario();
    }

    public Long getIdUsuarioB() {
        return this.usuarioB.getIdUsuario();
    }
}