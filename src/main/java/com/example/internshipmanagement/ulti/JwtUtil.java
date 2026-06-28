package com.example.internshipmanagement.ulti;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            if (token == null || token.isBlank()) {
                log.warn("JWT token rong hoac null");
                return false;
            }
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("JWT token da het han: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("JWT token khong dung dinh dang: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token khong duoc ho tro: {}", e.getMessage());
        } catch (SignatureException e) {
            log.error("Chu ky JWT khong hop le: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT token khong hop le: {}", e.getMessage());
        }
        return false;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
