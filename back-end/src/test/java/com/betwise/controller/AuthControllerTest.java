package com.betwise.controller;

import com.betwise.dto.LoginResponse;
import com.betwise.dto.RefreshTokenResponse;
import com.betwise.dto.UserResponse;
import com.betwise.model.User;
import com.betwise.repository.UserRepository;
import com.betwise.service.AuthService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.betwise.security.JwtService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void loginShouldReturnTokensAndUser() throws Exception {

        User user = new User();

        user.setId(1L);
        user.setName("Harry");
        user.setEmail("harry@gmail.com");

        UserResponse userResponse = new UserResponse(user);

        LoginResponse response = new LoginResponse(
                "access-token",
                "refresh-token",
                userResponse);

        when(authService.login(any()))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "harry@gmail.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .value("access-token"))
                .andExpect(jsonPath("$.refreshToken")
                        .value("refresh-token"))
                .andExpect(jsonPath("$.user.id")
                        .value(1))
                .andExpect(jsonPath("$.user.name")
                        .value("Harry"))
                .andExpect(jsonPath("$.user.email")
                        .value("harry@gmail.com"));
    }

    @Test
    void refreshTokenShouldReturnNewAccessToken() throws Exception {

        RefreshTokenResponse response = new RefreshTokenResponse("new-access-token");

        when(authService.refreshToken(any()))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "refresh-token"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .value("new-access-token"));
    }
}