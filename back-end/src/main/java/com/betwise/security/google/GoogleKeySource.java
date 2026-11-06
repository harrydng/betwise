package com.betwise.security.google;

import java.security.Key;
import java.util.Optional;

/**
 * Looks up the public key Google used to sign an ID token, by the token's
 * `kid` header.
 */
public interface GoogleKeySource {

    Optional<Key> findKey(String keyId);
}
