package br.com.atletahub.atletahub_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Pedido de redefinição de senha. Guarda o hash (SHA-256) do código enviado por e-mail, nunca o código.
 * Vale por pouco tempo e só pode ser usado uma vez.
 */
@Table(name = "redefinicao_senha")
@Entity(name = "RedefinicaoSenha")
@Getter
@Setter
@NoArgsConstructor
public class RedefinicaoSenha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_redefinicao")
    private Long id;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "token_hash", nullable = false, length = 64, unique = true)
    private String tokenHash;

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    @Column(name = "usada_em")
    private Instant usadaEm;

    public RedefinicaoSenha(Long idUsuario, String tokenHash, Instant criadaEm, Instant expiraEm) {
        this.idUsuario = idUsuario;
        this.tokenHash = tokenHash;
        this.criadaEm = criadaEm;
        this.expiraEm = expiraEm;
    }
}
