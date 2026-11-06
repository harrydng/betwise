package com.betwise.security.google;

import io.jsonwebtoken.security.Jwk;
import io.jsonwebtoken.security.JwkSet;
import io.jsonwebtoken.security.Jwks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fetches Google's public signing keys and caches them for as long as
 * Google's Cache-Control header allows. Google rotates keys every few days,
 * so an unknown key ID triggers one early refresh.
 */
@Component
public class HttpGoogleKeySource implements GoogleKeySource {

    private static final Logger log = LoggerFactory.getLogger(HttpGoogleKeySource.class);

    private static final URI KEYS_URL = URI.create("https://www.googleapis.com/oauth2/v3/certs");
    private static final Pattern MAX_AGE = Pattern.compile("max-age=(\\d+)");
    private static final Duration DEFAULT_TTL = Duration.ofHours(1);

    // Stops a flood of tokens with made-up key IDs from hammering Google.
    private static final Duration MIN_REFRESH_INTERVAL = Duration.ofMinutes(1);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private Map<String, Key> keys = Map.of();
    private Instant expiresAt = Instant.EPOCH;
    private Instant lastFetch = Instant.EPOCH;

    @Override
    public synchronized Optional<Key> findKey(String keyId) {
        if (keyId == null) {
            return Optional.empty();
        }

        Instant now = Instant.now();
        boolean expired = now.isAfter(expiresAt);
        boolean unknownAndRefreshAllowed = !keys.containsKey(keyId)
                && now.isAfter(lastFetch.plus(MIN_REFRESH_INTERVAL));

        if (expired || unknownAndRefreshAllowed) {
            refresh(now);
        }

        return Optional.ofNullable(keys.get(keyId));
    }

    private void refresh(Instant now) {
        lastFetch = now;

        try {
            HttpRequest request = HttpRequest.newBuilder(KEYS_URL)
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Google signing keys request returned {}", response.statusCode());
                return;
            }

            JwkSet jwkSet = Jwks.setParser().build().parse(response.body());
            Map<String, Key> parsed = new HashMap<>();

            for (Jwk<?> jwk : jwkSet.getKeys()) {
                if (jwk.getId() != null) {
                    parsed.put(jwk.getId(), jwk.toKey());
                }
            }

            keys = Map.copyOf(parsed);
            expiresAt = now.plus(cacheLifetime(response));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while fetching Google signing keys");
        } catch (Exception exception) {
            // Keep any keys we already have; verification fails closed if
            // the needed key is missing.
            log.warn("Couldn't fetch Google signing keys: {}", exception.getMessage());
        }
    }

    private static Duration cacheLifetime(HttpResponse<String> response) {
        return response.headers()
                .firstValue("Cache-Control")
                .map(MAX_AGE::matcher)
                .filter(Matcher::find)
                .map(matcher -> Duration.ofSeconds(Long.parseLong(matcher.group(1))))
                .orElse(DEFAULT_TTL);
    }
}
