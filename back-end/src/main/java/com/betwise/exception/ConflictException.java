package com.betwise.exception;

/**
 * Thrown when a request clashes with existing state, such as a duplicate
 * name or answering the same question twice. Returned as 409 Conflict.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
