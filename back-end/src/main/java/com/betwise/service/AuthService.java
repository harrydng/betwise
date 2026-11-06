package com.betwise.service;

import com.betwise.exception.AuthenticationFailedException;
import com.betwise.dto.GoogleLoginRequest;
import com.betwise.dto.LoginRequest;
import com.betwise.dto.LoginResponse;
import com.betwise.dto.RefreshTokenRequest;
import com.betwise.dto.RefreshTokenResponse;
import com.betwise.dto.UserResponse;
import com.betwise.model.User;
import com.betwise.repository.UserRepository;
import com.betwise.security.JwtService;
import com.betwise.security.LoginAttemptLimiter;
import com.betwise.security.google.GoogleIdTokenVerifier;
import com.betwise.security.google.GoogleIdentity;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String INVALID_CREDENTIALS = "Invalid email or password";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleIdTokenVerifier googleVerifier;
    private final LoginAttemptLimiter attemptLimiter;

    // Checked against when the email doesn't exist, so a missing account
    // takes as long to reject as a wrong password. Otherwise response
    // times would reveal which emails have accounts.
    private final String placeholderHash;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            GoogleIdTokenVerifier googleVerifier,
            LoginAttemptLimiter attemptLimiter) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.googleVerifier = googleVerifier;
        this.attemptLimiter = attemptLimiter;
        this.placeholderHash = passwordEncoder.encode("placeholder-password-for-timing");
    }

    public LoginResponse login(LoginRequest request) {

        if (request.getEmail() == null || request.getPassword() == null) {
            throw new AuthenticationFailedException(INVALID_CREDENTIALS);
        }

        String email = request.getEmail()
                .toLowerCase()
                .trim();

        attemptLimiter.checkAllowed(email);

        User user = userRepository.findByEmail(email).orElse(null);

        // Google-only accounts have no password, so they fall through to
        // the same "invalid" answer as an unknown email.
        String hash = user != null && user.hasPassword()
                ? user.getPasswordHash()
                : placeholderHash;

        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), hash);

        if (user == null || !user.hasPassword() || !passwordMatches) {
            attemptLimiter.recordFailure(email);
            throw new AuthenticationFailedException(INVALID_CREDENTIALS);
        }

        attemptLimiter.recordSuccess(email);

        return issueTokens(user);
    }

    /**
     * Signs in with a Google ID token, creating the account on first use.
     * An existing account with the same verified email is linked to the
     * Google account rather than duplicated.
     */
    @Transactional
    public LoginResponse loginWithGoogle(GoogleLoginRequest request) {

        GoogleIdentity identity = googleVerifier.verify(request.idToken(), request.nonce());

        User user = userRepository.findByGoogleSubject(identity.subject()).orElse(null);

        if (user == null) {
            user = userRepository.findByEmail(identity.email()).orElse(null);

            if (user != null && user.getGoogleSubject() != null) {
                // The email belongs to an account linked to a different
                // Google account.
                throw new AuthenticationFailedException(
                        "This email is already linked to a different Google account.");
            }

            if (user == null) {
                user = new User();
                user.setEmail(identity.email());
                user.setName(displayName(identity));
            }

            // Safe to link: Google has verified the user owns this email,
            // which the verifier requires.
            user.setGoogleSubject(identity.subject());
            user = userRepository.save(user);
        }

        return issueTokens(user);
    }

    public RefreshTokenResponse refreshToken(
            RefreshTokenRequest request) {

        String refreshToken = request.getRefreshToken();

        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            throw new AuthenticationFailedException(
                    "Invalid or expired refresh token");
        }

        Long userId = jwtService.extractUserIdFromRefreshToken(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationFailedException("Invalid or expired refresh token"));

        String newAccessToken = jwtService.generateAccessToken(user);

        return new RefreshTokenResponse(newAccessToken);
    }

    private LoginResponse issueTokens(User user) {
        return new LoginResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
                new UserResponse(user));
    }

    private static String displayName(GoogleIdentity identity) {
        if (identity.name() != null && !identity.name().isBlank()) {
            return identity.name().trim();
        }

        // Fall back to the part of the email before the @.
        return identity.email().substring(0, identity.email().indexOf('@'));
    }
}
