package br.com.atletahub.atletahub_backend.model;

import br.com.atletahub.atletahub_backend.enums.TipoMatch;
import jakarta.persistence.*;
import lombok.AllArgsConstructor; // Removido, pois construtor @AllArgsConstructor não será usado para entidade com construtor personalizado
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Table(name = "match_table", uniqueConstraints = { // Adicionando unique constraint
        @UniqueConstraint(columnNames = {"id_usuario_a", "id_usuario_b"}) // Garante que não haja match duplicado para o mesmo par
})
@Entity(name = "Match")
@Getter
@NoArgsConstructor // Necessário para JPA
@EqualsAndHashCode(of = "id")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_match")
    private Long id;

    // Relacionamento ManyToOne com a entidade Usuario para o primeiro usuário do match
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_a", referencedColumnName = "id_usuario", nullable = false)
    private Usuario usuarioA;

    // Relacionamento ManyToOne com a entidade Usuario para o segundo usuário do match
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_b", referencedColumnName = "id_usuario", nullable = false)
    private Usuario usuarioB;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_match", nullable = false)
    private TipoMatch tipoMatch;

    @Column(name = "data_match", nullable = false)
    private LocalDateTime dataMatch;

    // Construtor para criar um novo Match
    public Match(Usuario usuario1, Usuario usuario2, TipoMatch tipoMatch) {
        // Para garantir consistência e evitar duplicatas (ex: (A,B) e (B,A)),
        // sempre armazene o usuário com menor ID em usuarioA e o maior em usuarioB.

        if (usuario1.getIdUsuario() < usuario2.getIdUsuario()) {
            this.usuarioA = usuario1;
            this.usuarioB = usuario2;
        } else {
            this.usuarioA = usuario2;
            this.usuarioB = usuario1;
        }
        this.tipoMatch = tipoMatch;
        this.dataMatch = LocalDateTime.now(); // Define a data/hora atual automaticamente
    }

    // Como o match é criado automaticamente e não atualizado, não precisamos de um método 'atualizar'.
    // Getter para o ID do usuário A (útil para DTOs e lógica)

    public Long getIdUsuarioA() {
        return this.usuarioA.getIdUsuario();
    }

    // Getter para o ID do usuário B (útil para DTOs e lógica)

    public Long getIdUsuarioB() {
        return this.usuarioB.getIdUsuario();
    }
}