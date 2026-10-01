package br.com.atletahub.atletahub_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Table(name = "usuario")
@Entity(name = "Usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idUsuario")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @NotBlank
    private String nome;

    @NotBlank @Email
    private String email;

    @Column(name = "senha_hash")
    @NotBlank
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_usuario")
    @NotNull
    private TipoUsuario tipoUsuario;

    // --- NOVO CAMPO DE IDIOMA (V3 Migration) ---
    @Column(name = "idioma_preferencia")
    private String idiomaPreferencia;

    // --- LOCALIZAÇÃO (colunas já existem desde a V1; faltava mapear na entidade) ---
    @Column(name = "cidade", length = 100)
    private String cidade;

    @Column(name = "estado", length = 100)
    private String estado;

    // --- IDADE MÍNIMA E ACEITE DOS TERMOS (V7 Migration) ---
    // Nula só em contas criadas antes da V7 (o app pede a data no próximo acesso).
    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "termos_aceitos_em")
    private Instant termosAceitosEm;

    @Column(name = "termos_versao", length = 20)
    private String termosVersao;

    // --- CONSTRUTOR ATUALIZADO ---
    // Agora exige o idioma na criação
    public Usuario(String nome, String email, String senha, TipoUsuario tipoUsuario, String idiomaPreferencia) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.tipoUsuario = tipoUsuario;
        this.idiomaPreferencia = idiomaPreferencia;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.tipoUsuario == TipoUsuario.ADMIN) {
            return List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_ATLETA"),
                    new SimpleGrantedAuthority("ROLE_MARCA")
            );
        } else if (this.tipoUsuario == TipoUsuario.ATLETA) {
            return List.of(new SimpleGrantedAuthority("ROLE_ATLETA"));
        } else {
            return List.of(new SimpleGrantedAuthority("ROLE_MARCA"));
        }
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // E-mail fora do toString (LGPD: evita dado pessoal em log).
    @Override
    public String toString() {
        return "Usuario{" +
                "idUsuario=" + idUsuario +
                ", tipoUsuario=" + tipoUsuario +
                ", idiomaPreferencia='" + idiomaPreferencia + '\'' +
                '}';
    }
}
