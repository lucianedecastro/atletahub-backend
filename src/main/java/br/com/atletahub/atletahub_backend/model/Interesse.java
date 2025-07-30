package br.com.atletahub.atletahub_backend.model;

import br.com.atletahub.atletahub_backend.enums.TipoInteresse;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Table(name = "interesse")
@Entity(name = "Interesse")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Interesse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_interesse")
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_origem", referencedColumnName = "id_usuario", nullable = false)
    private Usuario origem;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_destino", referencedColumnName = "id_usuario", nullable = false)
    private Usuario destino;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_interesse", nullable = false)
    private TipoInteresse tipoInteresse;

    @Column(name = "data_envio", nullable = false)
    private LocalDateTime dataEnvio;


    public Interesse(Usuario origem, Usuario destino, TipoInteresse tipoInteresse) {
        this.origem = origem;
        this.destino = destino;
        this.tipoInteresse = tipoInteresse;
        this.dataEnvio = LocalDateTime.now();
    }


    public void atualizar(TipoInteresse novoTipoInteresse) {
        if (novoTipoInteresse != null) {
            this.tipoInteresse = novoTipoInteresse;
        }

    }
}
