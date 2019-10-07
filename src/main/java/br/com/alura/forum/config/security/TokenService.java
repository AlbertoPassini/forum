package br.com.alura.forum.config.security;

import br.com.alura.forum.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TokenService {
    @Value("${forum.jwt.expiration}") // Through expression lenguage this annotation allows Spring to get the values in aplication.properties
    private String expiration;

    @Value("${forum.jwt.secret}")
    private String secret;

    public String gerarToken(Authentication authentication){
        Usuario logado = (Usuario) authentication.getPrincipal(); //Recovers the user logged
        Date hoje = new Date();
        Date dataExpiracao = new Date(hoje.getTime() + Long.parseLong(expiration));
        return Jwts.builder() //Jwts.builder() makes possible to configure the token
                .setIssuer("API do Fórum da Alura") //What is the application that is creating the token
                .setSubject(logado.getId().toString()) //Who is the user that owns this token
                .setIssuedAt(hoje) //When the token was created
                .setExpiration(dataExpiracao) //When the token expires
                .signWith(SignatureAlgorithm.HS256, secret) //The algorithm to encrypt and the password
                .compact(); //Converts the token in a String
    }

    public boolean isTokenValido(String token) {
        try {
            Jwts.parser().setSigningKey(this.secret).parseClaimsJws(token);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    public Long getIdUsuario(String token) {
        Claims claims = Jwts.parser().setSigningKey(this.secret).parseClaimsJws(token).getBody();
        return Long.parseLong(claims.getSubject());
    }
}
