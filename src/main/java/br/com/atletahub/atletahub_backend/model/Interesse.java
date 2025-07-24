package br.com.atletahub.atletahub_backend.model;

import br.com.atletahub.atletahub_backend.enums.TipoInteresse;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime; // Usaremos LocalDateTime para data_envio

@Table(name = "interesse")
@Entity(name = "Interesse")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Interesse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_interesse") // Mapeia para o nome da coluna no banco
    private Long id; // Nome do atributo em camelCase, mas mapeado para id_interesse

    // Relacionamento ManyToOne com a entidade Usuario para o usuário de origem
    // 'id_usuario' é o nome da coluna no BD da tabela 'usuario'
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_origem", referencedColumnName = "id_usuario", nullable = false)
    private Usuario origem;

    // Relacionamento ManyToOne com a entidade Usuario para o usuário de destino
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_destino", referencedColumnName = "id_usuario", nullable = false)
    private Usuario destino;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_interesse", nullable = false) // Mapeia para o nome da coluna e não pode ser nulo
    private TipoInteresse tipoInteresse; // Nome do atributo em camelCase

    @Column(name = "data_envio", nullable = false) // Mapeia para o nome da coluna e não pode ser nulo
    private LocalDateTime dataEnvio; // Usamos LocalDateTime para o tipo DATETIME no MySQL

    // Construtor para criar um novo Interesse
    public Interesse(Usuario origem, Usuario destino, TipoInteresse tipoInteresse) {
        this.origem = origem;
        this.destino = destino;
        this.tipoInteresse = tipoInteresse;
        this.dataEnvio = LocalDateTime.now(); // Define a data/hora atual automaticamente
    }

    // Métodos para atualizar (se necessário), embora Interesses geralmente não sejam atualizados, mas criados ou excluídos
    public void atualizar(TipoInteresse novoTipoInteresse) {
        if (novoTipoInteresse != null) {
            this.tipoInteresse = novoTipoInteresse;
        }
        // dataEnvio geralmente não é atualizada, representa o momento da criação
    }
}
