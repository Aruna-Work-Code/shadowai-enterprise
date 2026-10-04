package com.aigovernance.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT service responsible for:
 *
 * 1. Generating authentication tokens after successful login.
 * 2. Parsing and validating incoming JWT tokens.
 * 3. Reading the username and role from the token.
 *
 * The same configured secret is used for signing and verification.
 */
@Service
public class JwtService {

    private final String secret;
    private final long minutes;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-minutes}") long minutes
    ) {
        this.secret = secret;
        this.minutes = minutes;
    }

    /**
     * Generate a JWT for an authenticated user.
     *
     * @param username authenticated username
     * @param role application role
     * @return signed JWT token
     */
    public String generate(String username, String role) {
        return generate(username, role, null);
    }

    public String generate(String username, String role, Long tenantId) {

        Date now = new Date();

        Date expiration = new Date(
                now.getTime() + (minutes * 60_000)
        );

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .claim("tenantId", tenantId)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(
                        Keys.hmacShaKeyFor(
                                secret.getBytes(StandardCharsets.UTF_8)
                        )
                )
                .compact();
    }

    /**
     * Parse and validate a JWT.
     *
     * If the token is invalid, expired, or has an invalid signature,
     * JJWT throws an exception which is handled by JwtFilter.
     *
     * @param token JWT token without the "Bearer " prefix
     * @return validated JWT claims
     */
    public Claims parse(String token) {

        return Jwts.parser()
                .verifyWith(
                        Keys.hmacShaKeyFor(
                                secret.getBytes(StandardCharsets.UTF_8)
                        )
                )
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}