package com.betwise.security;

import com.betwise.exception.TooManyRequestsException;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class LoginAttemptLimiterTest {

    private static final String EMAIL = "harry@gmail.com";

    private final MutableClock clock = new MutableClock();
    private final LoginAttemptLimiter limiter = new LoginAttemptLimiter(clock);

    @Test
    void blocksAfterTooManyFailures() {

        for (int i = 0; i < LoginAttemptLimiter.MAX_FAILURES; i++) {
            limiter.checkAllowed(EMAIL);
            limiter.recordFailure(EMAIL);
        }

        assertThrows(TooManyRequestsException.class, () -> limiter.checkAllowed(EMAIL));
    }

    @Test
    void countsEmailsCaseInsensitively() {

        for (int i = 0; i < LoginAttemptLimiter.MAX_FAILURES; i++) {
            limiter.recordFailure(" HARRY@gmail.com ");
        }

        assertThrows(TooManyRequestsException.class, () -> limiter.checkAllowed(EMAIL));
    }

    @Test
    void unblocksOnceTheWindowPasses() {

        for (int i = 0; i < LoginAttemptLimiter.MAX_FAILURES; i++) {
            limiter.recordFailure(EMAIL);
        }

        clock.advance(LoginAttemptLimiter.WINDOW.plusSeconds(1));

        assertDoesNotThrow(() -> limiter.checkAllowed(EMAIL));
    }

    @Test
    void successResetsTheCount() {

        for (int i = 0; i < LoginAttemptLimiter.MAX_FAILURES - 1; i++) {
            limiter.recordFailure(EMAIL);
        }

        limiter.recordSuccess(EMAIL);
        limiter.recordFailure(EMAIL);

        assertDoesNotThrow(() -> limiter.checkAllowed(EMAIL));
    }

    @Test
    void otherEmailsAreUnaffected() {

        for (int i = 0; i < LoginAttemptLimiter.MAX_FAILURES; i++) {
            limiter.recordFailure(EMAIL);
        }

        assertDoesNotThrow(() -> limiter.checkAllowed("someone@else.com"));
    }

    private static final class MutableClock extends Clock {

        private Instant now = Instant.parse("2026-11-06T12:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }
    }
}
