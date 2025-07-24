package br.com.atletahub.atletahub_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table(name = "mensagem") // Nome da tabela no banco de dados
@Entity(name = "Mensagem") // Nome da entidade para JPA
@Getter // Lombok para getters
@NoArgsConstructor // Lombok para construtor sem argumentos
@AllArgsConstructor // Lombok para construtor com todos os argumentos
@EqualsAndHashCode(of = "id") // Lombok para equals e hashCode baseado no ID
public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensagem") // Mapeia para a coluna id_mensagem
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Muitas mensagens para um Match
    @JoinColumn(name = "id_match", nullable = false) // Coluna da chave estrangeira id_match
    private Match match; // Objeto Match associado à mensagem

    @ManyToOne(fetch = FetchType.LAZY) // Muitas mensagens para um remetente (usuário)
    @JoinColumn(name = "id_remetente", nullable = false) // Coluna da chave estrangeira id_remetente
    private Usuario remetente; // Objeto Usuario que enviou a mensagem

    @Column(name = "texto", columnDefinition = "TEXT", nullable = false) // Coluna para o texto da mensagem
    private String texto;

    @Column(name = "data_envio", nullable = false) // Coluna para a data e hora de envio
    private LocalDateTime dataEnvio;

    // Construtor para criar uma nova mensagem (usado no serviço)
    public Mensagem(Match match, Usuario remetente, String texto) {
        this.match = match;
        this.remetente = remetente;
        this.texto = texto;
        this.dataEnvio = LocalDateTime.now(); // Define a data de envio automaticamente
    }
}
