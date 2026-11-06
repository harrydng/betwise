package com.betwise.security.google;

/**
 * The parts of a verified Google ID token Betwise uses.
 *
 * @param subject Google's stable, unique ID for the account
 */
public record GoogleIdentity(
        String subject,
        String email,
        String name
) {
}
