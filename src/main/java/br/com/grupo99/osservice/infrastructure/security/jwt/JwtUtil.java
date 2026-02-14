package br.com.grupo99.osservice.infrastructure.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration.ms}")
    private long jwtExpiration;

    /**
     * Extrai o nome de utilizador do token JWT.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrai o pessoaId do token JWT.
     */
    public String extractPessoaId(String token) {
        String pessoaId = extractClaim(token, claims -> claims.get("pessoaId", String.class));
        if (pessoaId == null || pessoaId.trim().isEmpty()) {
            throw new IllegalArgumentException("Token JWT não contém claim 'pessoaId'");
        }
        return pessoaId;
    }

    /**
     * Extrai o número de documento do token JWT.
     */
    public String extractNumeroDocumento(String token) {
        String numeroDocumento = extractClaim(token, claims -> claims.get("numeroDocumento", String.class));
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            throw new IllegalArgumentException("Token JWT não contém claim 'numeroDocumento'");
        }
        return numeroDocumento;
    }

    /**
     * Extrai o tipo de pessoa do token JWT.
     */
    public String extractTipoPessoa(String token) {
        String tipoPessoa = extractClaim(token, claims -> claims.get("tipoPessoa", String.class));
        if (tipoPessoa == null || tipoPessoa.trim().isEmpty()) {
            throw new IllegalArgumentException("Token JWT não contém claim 'tipoPessoa'");
        }
        return tipoPessoa;
    }

    /**
     * Extrai o cargo do token JWT.
     * Retorna null se o claim não existir (campo opcional).
     */
    public String extractCargo(String token) {
        return extractClaim(token, claims -> claims.get("cargo", String.class));
    }

    /**
     * Extrai o perfil do token JWT.
     * 
     * @param token Token JWT
     * @return Perfil do usuário (CLIENTE, MECANICO, ADMIN)
     * @throws IllegalArgumentException se o perfil não estiver presente no token
     */
    public String extractPerfil(String token) {
        String perfil = extractClaim(token, claims -> claims.get("perfil", String.class));
        if (perfil == null || perfil.trim().isEmpty()) {
            throw new IllegalArgumentException("Token JWT não contém claim 'perfil'");
        }
        return perfil;
    }

    /**
     * Extrai todas as claims necessárias e cria um JwtUserDetails.
     * 
     * @param token Token JWT válido
     * @return JwtUserDetails com informações do usuário
     */
    public JwtUserDetails extractUserDetails(String token) {
        String username = extractUsername(token);
        String pessoaId = extractPessoaId(token);
        String numeroDocumento = extractNumeroDocumento(token);
        String tipoPessoa = extractTipoPessoa(token);
        String cargo = extractCargo(token);
        String perfil = extractPerfil(token);

        return JwtUserDetails.from(username, pessoaId, numeroDocumento, tipoPessoa, cargo, perfil);
    }

    /**
     * Extrai uma reivindicação específica do token JWT usando uma função de
     * resolução.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Gera um token JWT para um utilizador.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Gera um token JWT com reivindicações extras.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Valida se um token JWT é válido para um determinado utilizador.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] decodedKey = java.util.Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(decodedKey);
    }
}
