package com.betwise.controller;

import com.betwise.dto.LoginRequest;
import com.betwise.dto.LoginResponse;
import com.betwise.dto.RefreshTokenRequest;
import com.betwise.dto.RefreshTokenResponse;
import com.betwise.service.AuthService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            @RequestBody RefreshTokenRequest request) {

        RefreshTokenResponse response = authService.refreshToken(request);

        return ResponseEntity.ok(response);
    }
}