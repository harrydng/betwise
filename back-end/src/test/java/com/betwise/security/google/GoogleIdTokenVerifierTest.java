package com.betwise.security.google;

import com.betwise.exception.AuthenticationFailedException;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GoogleIdTokenVerifierTest {

    private static final String CLIENT_ID = "123-ios.apps.googleusercontent.com";
    private static final String KEY_ID = "test-key";
    private static final String NONCE = "nonce-abc";

    private final KeyPair googleKeys = rsaKeyPair();
    private final KeyPair attackerKeys = rsaKeyPair();

    private final GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier(
            keyId -> KEY_ID.equals(keyId) ? Optional.of(googleKeys.getPublic()) : Optional.empty(),
            CLIENT_ID);

    @Test
    void acceptsAValidToken() {

        GoogleIdentity identity = verifier.verify(validToken().compact(), NONCE);

        assertEquals("google-sub-1", identity.subject());
        assertEquals("harry@gmail.com", identity.email());
        assertEquals("Harry Duong", identity.name());
    }

    @Test
    void rejectsATokenSignedByAnotherKey() {

        String forged = validToken().signWith(attackerKeys.getPrivate()).compact();

        assertThrows(AuthenticationFailedException.class, () -> verifier.verify(forged, NONCE));
    }

    @Test
    void rejectsAnUnknownKeyId() {

        String token = validToken().header().keyId("other-key").and().compact();

        assertThrows(AuthenticationFailedException.class, () -> verifier.verify(token, NONCE));
    }

    @Test
    void rejectsATokenForAnotherApp() {

        String token = validToken().audience().single("someone-elses-client-id").compact();

        assertThrows(AuthenticationFailedException.class, () -> verifier.verify(token, NONCE));
    }

    @Test
    void rejectsAnotherIssuer() {

        String token = validToken().issuer("https://evil.example.com").compact();

        assertThrows(AuthenticationFailedException.class, () -> verifier.verify(token, NONCE));
    }

    @Test
    void rejectsAnExpiredToken() {

        String token = validToken()
                .issuedAt(Date.from(Instant.now().minusSeconds(7200)))
                .expiration(Date.from(Instant.now().minusSeconds(3600)))
                .compact();

        assertThrows(AuthenticationFailedException.class, () -> verifier.verify(token, NONCE));
    }

    @Test
    void rejectsAReplayedTokenWithTheWrongNonce() {

        String token = validToken().compact();

        assertThrows(AuthenticationFailedException.class, () -> verifier.verify(token, "different-nonce"));
        assertThrows(AuthenticationFailedException.class, () -> verifier.verify(token, null));
    }

    @Test
    void rejectsAnUnverifiedEmail() {

        String token = validToken().claim("email_verified", false).compact();

        AuthenticationFailedException error = assertThrows(
                AuthenticationFailedException.class,
                () -> verifier.verify(token, NONCE));

        assertTrue(error.getMessage().contains("Verify"));
    }

    @Test
    void rejectsAnUnsignedToken() {

        String unsigned = Jwts.builder()
                .issuer("https://accounts.google.com")
                .audience().single(CLIENT_ID)
                .subject("google-sub-1")
                .claim("email", "harry@gmail.com")
                .claim("email_verified", true)
                .claim("nonce", NONCE)
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .compact();

        assertThrows(AuthenticationFailedException.class, () -> verifier.verify(unsigned, NONCE));
    }

    @Test
    void refusesToRunWithoutAClientId() {

        GoogleIdTokenVerifier unconfigured = new GoogleIdTokenVerifier(keyId -> Optional.empty(), " ");

        assertFalse(unconfigured.isConfigured());
        assertThrows(IllegalArgumentException.class, () -> unconfigured.verify("token", NONCE));
    }

    // MARK: - Helpers

    private JwtBuilder validToken() {
        return Jwts.builder()
                .header().keyId(KEY_ID).and()
                .issuer("https://accounts.google.com")
                .audience().single(CLIENT_ID)
                .subject("google-sub-1")
                .claim("email", "Harry@Gmail.com")
                .claim("email_verified", true)
                .claim("name", "Harry Duong")
                .claim("nonce", NONCE)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(googleKeys.getPrivate());
    }

    private static KeyPair rsaKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
