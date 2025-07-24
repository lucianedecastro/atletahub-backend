package br.com.atletahub.atletahub_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Table(name = "usuario")
@Entity(name = "Usuario")
@Getter
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
    private TipoUsuario tipoUsuario; // ATLETA, MARCA, ADMIN


    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PerfilAtleta perfilAtleta;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PerfilMarca perfilMarca;


    public Usuario(@NotBlank String nome, @NotBlank @Email String email, @NotBlank String senha, @NotNull TipoUsuario tipoUsuario) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.tipoUsuario = tipoUsuario;
    }

    // Métodos para o Spring Security (UserDetails)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.tipoUsuario == TipoUsuario.ADMIN) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_ATLETA"), new SimpleGrantedAuthority("ROLE_MARCA"));
        } else if (this.tipoUsuario == TipoUsuario.ATLETA) {
            return List.of(new SimpleGrantedAuthority("ROLE_ATLETA"));
        } else { // MARCA
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
}