package com.betwise.exception;

/**
 * Thrown when login credentials or a refresh token are rejected.
 * Returned as 401 Unauthorized.
 */
public class AuthenticationFailedException extends RuntimeException {

    public AuthenticationFailedException(String message) {
        super(message);
    }
}
