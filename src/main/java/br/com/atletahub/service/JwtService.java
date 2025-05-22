package br.com.atletahub.service;

import br.com.atletahub.model.Usuario; // Certifique-se de que esta importação está correta
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys; // Importar Keys para chaves seguras
import org.springframework.beans.factory.annotation.Value; // Adicione este import
import org.springframework.security.core.userdetails.UserDetails; // Adicione este import
import org.springframework.stereotype.Service;

import java.security.Key; // Adicione este import
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    // A chave secreta será lida do application.properties
    @Value("${jwt.secret-key}")
    private String secretKeyString;

    // O tempo de expiração será lido do application.properties
    @Value("${jwt.expiration-time}")
    private long expirationTime;

    private Key getSigningKey() {
        // Decodifica a chave secreta base64 e a converte para Key
        return Keys.hmacShaKeyFor(secretKeyString.getBytes());
    }

    public String gerarToken(Usuario usuario) {
        return Jwts.builder()
                .setSubject(usuario.getEmail())
                .claim("id", usuario.getIdUsuario())
                .claim("tipo", usuario.getTipoUsuario())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // Usar expirationTime
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extrai o subject (username/email) do token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extrai a data de expiração do token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extrai qualquer "claim" do token usando uma função de resolução
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extrai todas as "claims" do token
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Verifica se o token expirou
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Valida o token comparando o username e a data de expiração
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}