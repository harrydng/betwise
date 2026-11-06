package com.betwise.security.google;

import com.betwise.exception.AuthenticationFailedException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.LocatorAdapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Checks that a Google ID token is genuine and meant for Betwise before
 * the backend trusts the identity in it:
 *
 * - signed by one of Google's current keys (RS256)
 * - issued by Google
 * - addressed to one of Betwise's OAuth client IDs
 * - not expired
 * - for a verified email address
 * - carrying the nonce the app generated for this sign-in, so a token
 *   captured from another sign-in can't be replayed
 *
 * Follows https://developers.google.com/identity/sign-in/ios/backend-auth
 */
@Component
public class GoogleIdTokenVerifier {

    private static final Set<String> ISSUERS = Set.of("accounts.google.com", "https://accounts.google.com");
    private static final String INVALID = "Google sign-in couldn't be verified. Please try again.";

    private final GoogleKeySource keySource;
    private final Set<String> clientIds;

    public GoogleIdTokenVerifier(
            GoogleKeySource keySource,
            @Value("${betwise.google.client-ids:}") String clientIds) {

        this.keySource = keySource;
        this.clientIds = Arrays.stream(clientIds.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean isConfigured() {
        return !clientIds.isEmpty();
    }

    public GoogleIdentity verify(String idToken, String expectedNonce) {
        if (!isConfigured()) {
            throw new IllegalArgumentException("Google sign-in isn't set up on this server yet");
        }

        if (idToken == null || idToken.isBlank()) {
            throw new AuthenticationFailedException(INVALID);
        }

        Claims claims;

        try {
            claims = Jwts.parser()
                    .keyLocator(new LocatorAdapter<Key>() {
                        @Override
                        protected Key locate(JwsHeader header) {
                            return keySource.findKey(header.getKeyId())
                                    .orElseThrow(() -> new JwtException("Unknown signing key"));
                        }
                    })
                    // Allows for small clock differences between servers.
                    .clockSkewSeconds(60)
                    .build()
                    .parseSignedClaims(idToken)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException exception) {
            throw new AuthenticationFailedException(INVALID);
        }

        if (!ISSUERS.contains(claims.getIssuer())) {
            throw new AuthenticationFailedException(INVALID);
        }

        Set<String> audience = claims.getAudience();

        if (audience == null || audience.stream().noneMatch(clientIds::contains)) {
            throw new AuthenticationFailedException(INVALID);
        }

        if (claims.getExpiration() == null) {
            throw new AuthenticationFailedException(INVALID);
        }

        if (expectedNonce == null || expectedNonce.isBlank()
                || !expectedNonce.equals(claims.get("nonce", String.class))) {
            throw new AuthenticationFailedException(INVALID);
        }

        String subject = claims.getSubject();
        String email = claims.get("email", String.class);

        if (subject == null || subject.isBlank() || email == null || email.isBlank()) {
            throw new AuthenticationFailedException(INVALID);
        }

        if (!Boolean.TRUE.equals(emailVerified(claims))) {
            throw new AuthenticationFailedException("Verify your Google account's email address, then try again.");
        }

        String name = claims.get("name", String.class);

        return new GoogleIdentity(subject, email.toLowerCase().trim(), name);
    }

    // Google sends a JSON boolean, but some older tokens used the string "true".
    private static Boolean emailVerified(Claims claims) {
        Object value = claims.get("email_verified");

        if (value instanceof Boolean verified) {
            return verified;
        }

        return value != null && "true".equalsIgnoreCase(value.toString());
    }
}
