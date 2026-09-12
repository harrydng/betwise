package com.betwise.service;

import com.betwise.dto.CreateUserRequest;
import com.betwise.dto.UpdatePasswordRequest;
import com.betwise.dto.UpdateUserRequest;
import com.betwise.dto.UserResponse;
import com.betwise.model.User;
import com.betwise.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

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
    void createUserShouldSaveUser() {

        CreateUserRequest request = new CreateUserRequest();

        request.setName("Harry");
        request.setEmail(" HARRY@GMAIL.COM ");
        request.setPassword("password123");

        when(userRepository.existsByEmail(
                "harry@gmail.com")).thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashed-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {

                    User saved = invocation.getArgument(0);

                    saved.setId(1L);

                    return saved;
                });

        UserResponse response = userService.createUser(request);

        assertEquals(
                "harry@gmail.com",
                response.getEmail());

        verify(passwordEncoder)
                .encode("password123");

        verify(userRepository)
                .save(any(User.class));
    }

    @Test
    void createUserShouldFailForDuplicateEmail() {

        CreateUserRequest request = new CreateUserRequest();

        request.setEmail("harry@gmail.com");

        when(userRepository.existsByEmail(
                "harry@gmail.com")).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(request));

        verify(userRepository, never())
                .save(any());
    }

    @Test
    void getUserShouldReturnUser() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        UserResponse response = userService.getUser(1L);

        assertEquals(1L, response.getId());
        assertEquals("Harry", response.getName());
    }

    @Test
    void getUserShouldFailWhenUserDoesNotExist() {

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUser(99L));
    }

    @Test
    void updateUserShouldUpdateName() {

        UpdateUserRequest request = new UpdateUserRequest();

        request.setName("Harry Duong");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        UserResponse response = userService.updateUser(
                1L,
                request);

        assertEquals(
                "Harry Duong",
                response.getName());

        verify(userRepository).save(user);
    }

    @Test
    void updatePasswordShouldChangePasswordWhenCurrentPasswordMatches() {

        UpdatePasswordRequest request = new UpdatePasswordRequest();

        request.setCurrentPassword("old-password");
        request.setNewPassword("new-password");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "old-password",
                "hashed-password")).thenReturn(true);

        when(passwordEncoder.encode("new-password"))
                .thenReturn("new-hashed-password");

        userService.updatePassword(
                1L,
                request);

        assertEquals(
                "new-hashed-password",
                user.getPasswordHash());

        verify(userRepository).save(user);
    }

    @Test
    void updatePasswordShouldFailWhenCurrentPasswordIsWrong() {

        UpdatePasswordRequest request = new UpdatePasswordRequest();

        request.setCurrentPassword("wrong-password");
        request.setNewPassword("new-password");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrong-password",
                "hashed-password")).thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.updatePassword(
                        1L,
                        request));

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any());
    }

    @Test
    void deleteUserShouldDeleteExistingUser() {

        when(userRepository.existsById(1L))
                .thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository)
                .deleteById(1L);
    }

    @Test
    void deleteUserShouldFailWhenUserDoesNotExist() {

        when(userRepository.existsById(99L))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.deleteUser(99L));

        verify(userRepository, never())
                .deleteById(anyLong());
    }

    @Test
    void updateUserShouldUpdateAllOptionalFields() {

        UpdateUserRequest request = new UpdateUserRequest();

        LocalDate newDateOfBirth = LocalDate.of(2003, 5, 15);

        request.setDateOfBirth(newDateOfBirth);
        request.setExperienceLevel(User.ExperienceLevel.INTERMEDIATE);
        request.setInvestmentGoal(User.InvestmentGoal.LONG_TERM_GROWTH);
        request.setRiskTolerance(User.RiskTolerance.BALANCED);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        UserResponse response = userService.updateUser(1L, request);

        assertEquals(
                newDateOfBirth,
                response.getDateOfBirth());

        assertEquals(
                User.ExperienceLevel.INTERMEDIATE,
                response.getExperienceLevel());

        assertEquals(
                User.InvestmentGoal.LONG_TERM_GROWTH,
                response.getInvestmentGoal());

        assertEquals(
                User.RiskTolerance.BALANCED,
                response.getRiskTolerance());

        verify(userRepository).save(user);
    }

    @Test
    void updateUserShouldLeaveFieldsUnchangedWhenRequestFieldsAreNull() {

        LocalDate originalDateOfBirth = LocalDate.of(2003, 1, 1);

        user.setDateOfBirth(originalDateOfBirth);
        user.setExperienceLevel(User.ExperienceLevel.BEGINNER);
        user.setInvestmentGoal(User.InvestmentGoal.INCOME);
        user.setRiskTolerance(User.RiskTolerance.AGGRESSIVE);

        UpdateUserRequest request = new UpdateUserRequest();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        UserResponse response = userService.updateUser(1L, request);

        assertEquals(
                originalDateOfBirth,
                response.getDateOfBirth());

        assertEquals(
                User.ExperienceLevel.BEGINNER,
                response.getExperienceLevel());

        assertEquals(
                User.InvestmentGoal.INCOME,
                response.getInvestmentGoal());

        assertEquals(
                User.RiskTolerance.AGGRESSIVE,
                response.getRiskTolerance());
    }

    @Test
    void getUserByEmailShouldReturnUser() {

        when(userRepository.findByEmail("harry@gmail.com"))
                .thenReturn(Optional.of(user));

        User result = userService.getUserByEmail(
                " HARRY@GMAIL.COM ");

        assertEquals(user, result);

        verify(userRepository)
                .findByEmail("harry@gmail.com");
    }

    @Test
    void getUserByEmailShouldFailWhenUserDoesNotExist() {

        when(userRepository.findByEmail("missing@gmail.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUserByEmail(
                        " MISSING@GMAIL.COM "));

        assertEquals(
                "User not found",
                exception.getMessage());
    }

    @Test
    void updateUserShouldFailWhenUserDoesNotExist() {

        UpdateUserRequest request = new UpdateUserRequest();

        request.setName("New Name");

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(
                        99L,
                        request));

        assertEquals(
                "User not found",
                exception.getMessage());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void updatePasswordShouldFailWhenUserDoesNotExist() {

        UpdatePasswordRequest request = new UpdatePasswordRequest();

        request.setCurrentPassword("old-password");
        request.setNewPassword("new-password");

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updatePassword(
                        99L,
                        request));

        assertEquals(
                "User not found",
                exception.getMessage());

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }
}