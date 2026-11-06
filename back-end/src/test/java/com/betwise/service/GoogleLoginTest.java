package com.betwise.service;

import com.betwise.dto.GoogleLoginRequest;
import com.betwise.dto.LoginRequest;
import com.betwise.dto.LoginResponse;
import com.betwise.exception.AuthenticationFailedException;
import com.betwise.exception.TooManyRequestsException;
import com.betwise.model.User;
import com.betwise.repository.UserRepository;
import com.betwise.security.JwtService;
import com.betwise.security.LoginAttemptLimiter;
import com.betwise.security.google.GoogleIdTokenVerifier;
import com.betwise.security.google.GoogleIdentity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleLoginTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private GoogleIdTokenVerifier googleVerifier;
    @Mock private LoginAttemptLimiter attemptLimiter;

    @InjectMocks
    private AuthService authService;

    private final GoogleLoginRequest request = new GoogleLoginRequest("id-token", "nonce");
    private final GoogleIdentity identity = new GoogleIdentity("google-sub-1", "harry@gmail.com", "Harry Duong");

    @BeforeEach
    void setUp() {
        lenient().when(jwtService.generateAccessToken(any())).thenReturn("access");
        lenient().when(jwtService.generateRefreshToken(any())).thenReturn("refresh");
        lenient().when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void firstGoogleSignInCreatesAnAccountWithoutAPassword() {

        when(googleVerifier.verify("id-token", "nonce")).thenReturn(identity);
        when(userRepository.findByGoogleSubject("google-sub-1")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("harry@gmail.com")).thenReturn(Optional.empty());

        LoginResponse response = authService.loginWithGoogle(request);

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());

        assertEquals("access", response.getAccessToken());
        assertEquals("Harry Duong", saved.getValue().getName());
        assertEquals("google-sub-1", saved.getValue().getGoogleSubject());
        assertFalse(saved.getValue().hasPassword());
        assertFalse(response.getUser().isHasPassword());
    }

    @Test
    void returningGoogleUserIsMatchedBySubjectNotEmail() {

        User existing = user("old-email@gmail.com", "hash");
        existing.setGoogleSubject("google-sub-1");

        when(googleVerifier.verify("id-token", "nonce")).thenReturn(identity);
        when(userRepository.findByGoogleSubject("google-sub-1")).thenReturn(Optional.of(existing));

        LoginResponse response = authService.loginWithGoogle(request);

        assertEquals("old-email@gmail.com", response.getUser().getEmail());
        verify(userRepository, never()).findByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void googleSignInLinksAnExistingPasswordAccountWithTheSameEmail() {

        User existing = user("harry@gmail.com", "hash");

        when(googleVerifier.verify("id-token", "nonce")).thenReturn(identity);
        when(userRepository.findByGoogleSubject("google-sub-1")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("harry@gmail.com")).thenReturn(Optional.of(existing));

        LoginResponse response = authService.loginWithGoogle(request);

        assertEquals("google-sub-1", existing.getGoogleSubject());
        assertTrue(existing.hasPassword());
        assertTrue(response.getUser().isGoogleLinked());
    }

    @Test
    void emailLinkedToADifferentGoogleAccountIsRejected() {

        User existing = user("harry@gmail.com", "hash");
        existing.setGoogleSubject("someone-else");

        when(googleVerifier.verify("id-token", "nonce")).thenReturn(identity);
        when(userRepository.findByGoogleSubject("google-sub-1")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("harry@gmail.com")).thenReturn(Optional.of(existing));

        assertThrows(AuthenticationFailedException.class, () -> authService.loginWithGoogle(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void invalidGoogleTokenIssuesNoTokens() {

        when(googleVerifier.verify("id-token", "nonce"))
                .thenThrow(new AuthenticationFailedException("bad"));

        assertThrows(AuthenticationFailedException.class, () -> authService.loginWithGoogle(request));
        verify(jwtService, never()).generateAccessToken(any());
    }

    // MARK: - Password sign-in

    @Test
    void googleOnlyAccountCantSignInWithAPassword() {

        User googleOnly = user("harry@gmail.com", null);
        googleOnly.setGoogleSubject("google-sub-1");

        when(userRepository.findByEmail("harry@gmail.com")).thenReturn(Optional.of(googleOnly));

        assertThrows(AuthenticationFailedException.class,
                () -> authService.login(loginRequest("harry@gmail.com", "anything123")));
        verify(attemptLimiter).recordFailure("harry@gmail.com");
    }

    @Test
    void blockedEmailIsRejectedBeforeCheckingThePassword() {

        doThrow(new TooManyRequestsException("slow down"))
                .when(attemptLimiter).checkAllowed("harry@gmail.com");

        assertThrows(TooManyRequestsException.class,
                () -> authService.login(loginRequest("harry@gmail.com", "password123")));
        verify(userRepository, never()).findByEmail(any());
    }

    private static User user(String email, String passwordHash) {
        User user = new User();
        user.setId(1L);
        user.setName("Harry");
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        return user;
    }

    private static LoginRequest loginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }
}
