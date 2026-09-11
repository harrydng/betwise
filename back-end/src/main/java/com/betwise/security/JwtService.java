package com.betwise.security;

import com.betwise.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.refresh-secret}")
    private String refreshSecret;

    private static final long ACCESS_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24;

    private static final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 30;

    private SecretKey getAccessSigningKey() {
        return Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8));
    }

    private SecretKey getRefreshSigningKey() {
        return Keys.hmacShaKeyFor(
                refreshSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + ACCESS_TOKEN_EXPIRATION))
                .signWith(getAccessSigningKey())
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + REFRESH_TOKEN_EXPIRATION))
                .signWith(getRefreshSigningKey())
                .compact();
    }

    public Claims extractAccessClaims(String token) {
        return Jwts.parser()
                .verifyWith(getAccessSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Claims extractRefreshClaims(String token) {
        return Jwts.parser()
                .verifyWith(getRefreshSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extractUserIdFromAccessToken(String token) {
        String subject = extractAccessClaims(token).getSubject();

        return Long.parseLong(subject);
    }

    public Long extractUserIdFromRefreshToken(String token) {
        String subject = extractRefreshClaims(token).getSubject();

        return Long.parseLong(subject);
    }

    public boolean isAccessTokenValid(String token) {
        try {
            Claims claims = extractAccessClaims(token);

            String type = claims.get("type", String.class);

            return "access".equals(type)
                    && claims.getExpiration().after(new Date());

        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRefreshTokenValid(String token) {
        try {
            Claims claims = extractRefreshClaims(token);

            String type = claims.get("type", String.class);

            return "refresh".equals(type)
                    && claims.getExpiration().after(new Date());

        } catch (Exception e) {
            return false;
        }
    }
}