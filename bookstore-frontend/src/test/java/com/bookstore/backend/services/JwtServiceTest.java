package com.bookstore.backend.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private SecretKey secretKey;
    private final String secret = "mySecretKeyForJwtTokenGenerationAndValidation1234567890";
    private final long expirationTime = 3600;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(secret, expirationTime);
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void shouldGenerateValidJwtToken() {
        String userId = "user-123";
        String email = "test@example.com";
        String firstName = "John";
        String lastName = "Doe";

        String token = jwtService.generateToken(userId, email, firstName, lastName);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void shouldIncludeUserDataInJwtClaims() {
        String userId = "user-123";
        String email = "test@example.com";
        String firstName = "John";
        String lastName = "Doe";

        String token = jwtService.generateToken(userId, email, firstName, lastName);

        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(userId);
        assertThat(claims.get("userId", String.class)).isEqualTo(userId);
        assertThat(claims.get("email", String.class)).isEqualTo(email);
        assertThat(claims.get("firstName", String.class)).isEqualTo(firstName);
        assertThat(claims.get("lastName", String.class)).isEqualTo(lastName);
    }

    @Test
    void shouldSetExpirationTime() {
        String userId = "user-123";
        String email = "test@example.com";
        String firstName = "John";
        String lastName = "Doe";

        String token = jwtService.generateToken(userId, email, firstName, lastName);

        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getExpiration()).isNotNull();
        assertThat(claims.getIssuedAt()).isNotNull();

        long expirationInSeconds = (claims.getExpiration().getTime() - claims.getIssuedAt().getTime()) / 1000;
        assertThat(expirationInSeconds).isEqualTo(3600L);
    }

    @Test
    void shouldGenerateSignedJwtToken() {
        String userId = "user-123";
        String email = "test@example.com";
        String firstName = "John";
        String lastName = "Doe";

        String token = jwtService.generateToken(userId, email, firstName, lastName);

        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims).isNotNull();
    }

    @Test
    void shouldExtractUserIdFromToken() {
        String userId = "user-123";
        String email = "test@example.com";
        String firstName = "John";
        String lastName = "Doe";

        String token = jwtService.generateToken(userId, email, firstName, lastName);
        String extractedUserId = jwtService.extractUserId(token);

        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    void shouldValidateValidToken() {
        String userId = "user-123";
        String email = "test@example.com";
        String firstName = "John";
        String lastName = "Doe";

        String token = jwtService.generateToken(userId, email, firstName, lastName);
        boolean isValid = jwtService.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        String invalidToken = "invalid.token.here";

        boolean isValid = jwtService.validateToken(invalidToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldReturnFalseForExpiredToken() {
        JwtService shortLivedJwtService = new JwtService(secret, -1);
        String userId = "user-123";
        String email = "test@example.com";
        String firstName = "John";
        String lastName = "Doe";

        String expiredToken = shortLivedJwtService.generateToken(userId, email, firstName, lastName);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean isValid = jwtService.validateToken(expiredToken);

        assertThat(isValid).isFalse();
    }
}
