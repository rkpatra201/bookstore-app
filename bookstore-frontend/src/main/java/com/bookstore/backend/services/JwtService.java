package com.bookstore.backend.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationTime;

    public JwtService(
            @Value("${jwt.secret:mySecretKeyForJwtTokenGenerationAndValidation1234567890}") String secret,
            @Value("${jwt.expiration:3600}") long expirationTime) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationTime = expirationTime;
    }

    public String generateToken(String userId, String email, String firstName, String lastName) {
        log.info("Generating JWT token for userId: {}", userId);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("firstName", firstName);
        claims.put("lastName", lastName);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime * 1000);

        String token = Jwts.builder()
                .subject(userId)
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();

        log.info("JWT token generated successfully for userId: {}", userId);
        return token;
    }

    public String extractUserId(String token) {
        log.debug("Extracting userId from JWT token");
        Claims claims = extractAllClaims(token);
        return claims.get("userId", String.class);
    }

    public boolean validateToken(String token) {
        try {
            log.debug("Validating JWT token");
            Claims claims = extractAllClaims(token);
            boolean isValid = !isTokenExpired(claims);
            log.debug("JWT token validation result: {}", isValid);
            return isValid;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.error("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }
}
