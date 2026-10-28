package com.betwise.service;

import com.betwise.exception.AuthenticationFailedException;
import com.betwise.dto.LoginRequest;
import com.betwise.dto.LoginResponse;
import com.betwise.dto.RefreshTokenRequest;
import com.betwise.dto.RefreshTokenResponse;
import com.betwise.dto.UserResponse;
import com.betwise.model.User;
import com.betwise.repository.UserRepository;
import com.betwise.security.JwtService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        String email = request.getEmail()
                .toLowerCase()
                .trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationFailedException(
                        "Invalid email or password"));

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash());

        if (!passwordMatches) {
            throw new AuthenticationFailedException(
                    "Invalid email or password");
        }

        String accessToken = jwtService.generateAccessToken(user);

        String refreshToken = jwtService.generateRefreshToken(user);

        return new LoginResponse(
                accessToken,
                refreshToken,
                new UserResponse(user));
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
}