package com.betwise.security;

import com.betwise.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    private User user;

    @BeforeEach
    void setUp() {

        jwtService = new JwtService();

        ReflectionTestUtils.setField(
                jwtService,
                "secret",
                "12345678901234567890123456789012345678901234567890"
        );

        ReflectionTestUtils.setField(
                jwtService,
                "refreshSecret",
                "09876543210987654321098765432109876543210987654321"
        );

        user = new User();

        user.setId(1L);
        user.setEmail("harry@gmail.com");
    }

    @Test
    void shouldGenerateValidAccessToken() {

        String token =
                jwtService.generateAccessToken(user);

        assertNotNull(token);

        assertTrue(
                jwtService.isAccessTokenValid(token)
        );
    }

    @Test
    void shouldExtractUserIdFromAccessToken() {

        String token =
                jwtService.generateAccessToken(user);

        Long userId =
                jwtService.extractUserIdFromAccessToken(
                        token
                );

        assertEquals(1L, userId);
    }

    @Test
    void shouldGenerateValidRefreshToken() {

        String token =
                jwtService.generateRefreshToken(user);

        assertNotNull(token);

        assertTrue(
                jwtService.isRefreshTokenValid(token)
        );
    }

    @Test
    void shouldExtractUserIdFromRefreshToken() {

        String token =
                jwtService.generateRefreshToken(user);

        Long userId =
                jwtService.extractUserIdFromRefreshToken(
                        token
                );

        assertEquals(1L, userId);
    }

    @Test
    void accessTokenShouldNotBeValidAsRefreshToken() {

        String accessToken =
                jwtService.generateAccessToken(user);

        assertFalse(
                jwtService.isRefreshTokenValid(
                        accessToken
                )
        );
    }

    @Test
    void refreshTokenShouldNotBeValidAsAccessToken() {

        String refreshToken =
                jwtService.generateRefreshToken(user);

        assertFalse(
                jwtService.isAccessTokenValid(
                        refreshToken
                )
        );
    }

    @Test
    void invalidTokenShouldReturnFalse() {

        assertFalse(
                jwtService.isAccessTokenValid(
                        "this-is-not-a-jwt"
                )
        );
    }
}