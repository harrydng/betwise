package com.betwise.service;

import com.betwise.dto.LoginRequest;
import com.betwise.dto.LoginResponse;
import com.betwise.dto.RefreshTokenRequest;
import com.betwise.dto.RefreshTokenResponse;
import com.betwise.model.User;
import com.betwise.repository.UserRepository;
import com.betwise.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();

        user.setId(1L);
        user.setName("Harry");
        user.setEmail("harry@gmail.com");
        user.setPasswordHash("hashed-password");
    }

    @Test
    void loginShouldReturnTokensWhenCredentialsAreCorrect() {

        LoginRequest request = new LoginRequest();
        request.setEmail(" HARRY@GMAIL.COM ");
        request.setPassword("password123");

        when(userRepository.findByEmail("harry@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password123",
                "hashed-password")).thenReturn(true);

        when(jwtService.generateAccessToken(user))
                .thenReturn("access-token");

        when(jwtService.generateRefreshToken(user))
                .thenReturn("refresh-token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);

        assertEquals(
                "access-token",
                response.getAccessToken());

        assertEquals(
                "refresh-token",
                response.getRefreshToken());

        assertEquals(
                "harry@gmail.com",
                response.getUser().getEmail());
    }

    @Test
    void loginShouldFailWhenEmailDoesNotExist() {

        LoginRequest request = new LoginRequest();

        request.setEmail("wrong@gmail.com");
        request.setPassword("password");

        when(userRepository.findByEmail("wrong@gmail.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request));

        assertEquals(
                "Invalid email or password",
                exception.getMessage());

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());
    }

    @Test
    void loginShouldFailWhenPasswordIsIncorrect() {

        LoginRequest request = new LoginRequest();

        request.setEmail("harry@gmail.com");
        request.setPassword("wrong-password");

        when(userRepository.findByEmail("harry@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrong-password",
                "hashed-password")).thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request));

        verify(jwtService, never())
                .generateAccessToken(any());

        verify(jwtService, never())
                .generateRefreshToken(any());
    }

    @Test
    void refreshTokenShouldReturnNewAccessToken() {

        RefreshTokenRequest request = new RefreshTokenRequest();

        request.setRefreshToken("refresh-token");

        when(jwtService.isRefreshTokenValid("refresh-token"))
                .thenReturn(true);

        when(jwtService.extractUserIdFromRefreshToken(
                "refresh-token")).thenReturn(1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(jwtService.generateAccessToken(user))
                .thenReturn("new-access-token");

        RefreshTokenResponse response = authService.refreshToken(request);

        assertEquals(
                "new-access-token",
                response.getAccessToken());
    }

    @Test
    void refreshTokenShouldFailWhenTokenIsInvalid() {

        RefreshTokenRequest request = new RefreshTokenRequest();

        request.setRefreshToken("bad-token");

        when(jwtService.isRefreshTokenValid("bad-token"))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.refreshToken(request));

        verify(userRepository, never())
                .findById(anyLong());
    }

    @Test
    void refreshTokenShouldFailWhenUserDoesNotExist() {

        RefreshTokenRequest request = new RefreshTokenRequest();

        request.setRefreshToken("valid-refresh-token");

        when(jwtService.isRefreshTokenValid(
                "valid-refresh-token")).thenReturn(true);

        when(jwtService.extractUserIdFromRefreshToken(
                "valid-refresh-token")).thenReturn(99L);

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.refreshToken(request));

        assertEquals(
                "User not found",
                exception.getMessage());

        verify(jwtService, never())
                .generateAccessToken(any());
    }
}