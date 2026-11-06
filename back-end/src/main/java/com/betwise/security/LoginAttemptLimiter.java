package com.betwise.security;

import com.betwise.exception.TooManyRequestsException;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Slows down password guessing. After MAX_FAILURES wrong passwords for an
 * email within WINDOW, further attempts for that email are refused until
 * the window passes. A successful sign-in clears the count.
 *
 * Counts are kept in memory, so they reset on restart and aren't shared
 * between multiple backend instances. Move this to Redis if the backend
 * ever runs on more than one server.
 */
@Component
public class LoginAttemptLimiter {

    static final int MAX_FAILURES = 5;
    static final Duration WINDOW = Duration.ofMinutes(15);

    private record Attempts(int failures, Instant windowStart) {
    }

    private final ConcurrentHashMap<String, Attempts> attempts = new ConcurrentHashMap<>();
    private final Clock clock;

    public LoginAttemptLimiter() {
        this(Clock.systemUTC());
    }

    LoginAttemptLimiter(Clock clock) {
        this.clock = clock;
    }

    /** Throws if this email has too many recent failures. */
    public void checkAllowed(String email) {
        Attempts current = attempts.get(key(email));

        if (current == null) {
            return;
        }

        Instant now = clock.instant();

        if (now.isAfter(current.windowStart().plus(WINDOW))) {
            attempts.remove(key(email), current);
            return;
        }

        if (current.failures() >= MAX_FAILURES) {
            long minutesLeft = Math.max(1,
                    Duration.between(now, current.windowStart().plus(WINDOW)).toMinutes());

            throw new TooManyRequestsException(
                    "Too many sign-in attempts. Try again in " + minutesLeft + " minutes.");
        }
    }

    public void recordFailure(String email) {
        Instant now = clock.instant();

        attempts.compute(key(email), (ignored, current) -> {
            if (current == null || now.isAfter(current.windowStart().plus(WINDOW))) {
                return new Attempts(1, now);
            }

            return new Attempts(current.failures() + 1, current.windowStart());
        });
    }

    public void recordSuccess(String email) {
        attempts.remove(key(email));
    }

    private static String key(String email) {
        return email == null ? "" : email.toLowerCase().trim();
    }
}
