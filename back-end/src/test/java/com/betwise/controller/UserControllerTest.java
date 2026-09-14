package com.betwise.controller;

import com.betwise.dto.CreateUserRequest;
import com.betwise.dto.UpdatePasswordRequest;
import com.betwise.dto.UpdateUserRequest;
import com.betwise.dto.UserResponse;
import com.betwise.model.User;
import com.betwise.repository.UserRepository;
import com.betwise.security.JwtService;
import com.betwise.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {

        user = new User();

        user.setId(1L);
        user.setName("Harry");
        user.setEmail("harry@gmail.com");

        userResponse = new UserResponse(user);
    }

    private UsernamePasswordAuthenticationToken authenticatedUser() {
        return new UsernamePasswordAuthenticationToken(
                user,
                null,
                Collections.emptyList()
        );
    }

    @Test
    void createUserShouldReturnCreatedUser() throws Exception {

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenReturn(userResponse);

        mockMvc.perform(
                post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Harry",
                                  "email": "harry@gmail.com",
                                  "password": "password123"
                                }
                                """)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Harry"))
        .andExpect(jsonPath("$.email").value("harry@gmail.com"));

        verify(userService)
                .createUser(any(CreateUserRequest.class));
    }

    @Test
    void getCurrentUserShouldReturnAuthenticatedUser() throws Exception {

        mockMvc.perform(
                get("/api/users/me")
                        .principal(authenticatedUser())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Harry"))
        .andExpect(jsonPath("$.email").value("harry@gmail.com"));
    }

    @Test
    void updateCurrentUserShouldReturnUpdatedUser() throws Exception {

        User updatedUser = new User();

        updatedUser.setId(1L);
        updatedUser.setName("Harry Duong");
        updatedUser.setEmail("harry@gmail.com");

        UserResponse updatedResponse =
                new UserResponse(updatedUser);

        when(userService.updateUser(
                eq(1L),
                any(UpdateUserRequest.class)
        )).thenReturn(updatedResponse);

        mockMvc.perform(
                patch("/api/users/me")
                        .principal(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Harry Duong"
                                }
                                """)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Harry Duong"))
        .andExpect(jsonPath("$.email").value("harry@gmail.com"));

        verify(userService)
                .updateUser(
                        eq(1L),
                        any(UpdateUserRequest.class)
                );
    }

    @Test
    void updatePasswordShouldReturnNoContent() throws Exception {

        doNothing()
                .when(userService)
                .updatePassword(
                        eq(1L),
                        any(UpdatePasswordRequest.class)
                );

        mockMvc.perform(
                patch("/api/users/me/password")
                        .principal(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "old-password",
                                  "newPassword": "new-password"
                                }
                                """)
        )
        .andExpect(status().isNoContent());

        verify(userService)
                .updatePassword(
                        eq(1L),
                        any(UpdatePasswordRequest.class)
                );
    }

    @Test
    void deleteCurrentUserShouldReturnNoContent() throws Exception {

        doNothing()
                .when(userService)
                .deleteUser(1L);

        mockMvc.perform(
                delete("/api/users/me")
                        .principal(authenticatedUser())
        )
        .andExpect(status().isNoContent());

        verify(userService)
                .deleteUser(1L);
    }
}