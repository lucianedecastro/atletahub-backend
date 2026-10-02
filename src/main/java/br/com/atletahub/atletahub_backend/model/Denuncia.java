package br.com.atletahub.atletahub_backend.model;

import br.com.atletahub.atletahub_backend.enums.MotivoDenuncia;
import br.com.atletahub.atletahub_backend.enums.StatusDenuncia;
import br.com.atletahub.atletahub_backend.enums.TipoAlvoDenuncia;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Table(name = "denuncia")
@Entity(name = "Denuncia")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Denuncia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_denuncia")
    private Long id;

    @Column(name = "id_denunciante", nullable = false)
    private Long idDenunciante;

    @Column(name = "id_denunciado", nullable = false)
    private Long idDenunciado;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_alvo", nullable = false, length = 20)
    private TipoAlvoDenuncia tipoAlvo;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", nullable = false, length = 30)
    private MotivoDenuncia motivo;

    @Column(name = "descricao", length = 1000)
    private String descricao;

    @Column(name = "referencia", length = 500)
    private String referencia;

    @Column(name = "trecho", columnDefinition = "TEXT")
    private String trecho;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusDenuncia status = StatusDenuncia.ABERTA;

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm = Instant.now();

    @Column(name = "resolvida_em")
    private Instant resolvidaEm;

    @Column(name = "resolvida_por")
    private Long resolvidaPor;

    @Column(name = "nota_admin", length = 1000)
    private String notaAdmin;
}
