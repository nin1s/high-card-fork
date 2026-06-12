package it.sara.demo.service.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * Utility per la gestione dei token JWT.
 * Fornisce metodi per la generazione, validazione ed estrazione dei claim dal token.
 * 
 * @author Alina Valega
 * @version 1.0
 */
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Genera un token JWT per lo username indicato, con ruolo di default ROLE_USER.
     *
     * @param username lo username da inserire come subject del token
     * @return il token JWT firmato
     */
    public String generateToken(String username) {
        return generateToken(username, List.of("ROLE_USER"));
    }

    /**
     * Genera un token JWT con una lista specifica di ruoli.
     * 
     * @param username Nome utente da inserire nel subject
     * @param roles Lista di ruoli (authority) dell'utente
     * @return Stringa rappresentante il token JWT
     */
    public String generateToken(String username, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Valida l'integrità, l'issuer e la scadenza del token JWT.
     * 
     * @param token Stringa del token da validare
     * @return true se il token è valido, false altrimenti
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Estrae lo username (subject) dal token JWT.
     * 
     * @param token Stringa del token JWT
     * @return Lo username contenuto nel token
     */
    public String extractUsername(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    /**
     * Estrae la lista dei ruoli dal claim "roles" del token JWT.
     * 
     * @param token Stringa del token JWT
     * @return Lista di ruoli estratti
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("roles", List.class);
    }
}