package com.betwise.exception;

import java.time.LocalDateTime;

/**
 * Error body for every failed request. Uses the same field names as Spring
 * Boot's default error JSON, plus a human-readable message.
 */
public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
