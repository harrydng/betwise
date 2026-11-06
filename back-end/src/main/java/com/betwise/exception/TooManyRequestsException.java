package com.betwise.exception;

/**
 * Thrown when a client should slow down, for example after repeated
 * failed sign-ins. Returned as 429 Too Many Requests.
 */
public class TooManyRequestsException extends RuntimeException {

    public TooManyRequestsException(String message) {
        super(message);
    }
}
