package com.betwise.exception;

/**
 * Thrown when a requested record doesn't exist or doesn't belong to the
 * current user. Returned as 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
