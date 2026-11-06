package com.betwise.dto;

/**
 * @param idToken the ID token Google returned to the app
 * @param nonce   the random value the app put in its Google sign-in
 *                request; it must match the token's `nonce` claim
 */
public record GoogleLoginRequest(
        String idToken,
        String nonce
) {
}
