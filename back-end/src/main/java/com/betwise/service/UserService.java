package com.betwise.service;

import com.betwise.dto.CreateUserRequest;
import com.betwise.dto.UpdatePasswordRequest;
import com.betwise.dto.UpdateUserRequest;
import com.betwise.dto.UserResponse;
import com.betwise.model.User;
import com.betwise.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creating a user
     * 
     * @param request
     * @return
     */
    public UserResponse createUser(CreateUserRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setDateOfBirth(request.getDateOfBirth());

        user.setExperienceLevel(request.getExperienceLevel());
        user.setInvestmentGoal(request.getInvestmentGoal());
        user.setRiskTolerance(request.getRiskTolerance());

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        user.setPasswordHash(hashedPassword);

        User savedUser = userRepository.save(user);

        return new UserResponse(savedUser);
    }

    /**
     * Retrieve the User from the Database given the ID
     * 
     * @param userId
     * @param request
     * @return
     */
    public UserResponse getUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return new UserResponse(user);
    }

    /**
     * Update the User with the information given
     * 
     * @param userId
     * @param request
     * @return
     */
    public UserResponse updateUser(
            Long userId,
            UpdateUserRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getName() != null) {
            user.setName(request.getName());
        }

        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }

        if (request.getExperienceLevel() != null) {
            user.setExperienceLevel(request.getExperienceLevel());
        }

        if (request.getInvestmentGoal() != null) {
            user.setInvestmentGoal(request.getInvestmentGoal());
        }

        if (request.getRiskTolerance() != null) {
            user.setRiskTolerance(request.getRiskTolerance());
        }

        User updatedUser = userRepository.save(user);

        return new UserResponse(updatedUser);
    }

    /**
     * Update the User's password and hash it to the database
     * 
     * @param userId
     * @param request
     */
    public void updatePassword(
            Long userId,
            UpdatePasswordRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean matches = passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPasswordHash());

        if (!matches) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        String newHashedPassword = passwordEncoder.encode(request.getNewPassword());

        user.setPasswordHash(newHashedPassword);

        userRepository.save(user);
    }

    /**
     * Deleting user account
     * 
     * @param userId
     */
    public void deleteUser(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }

        userRepository.deleteById(userId);
    }

    /**
     * Getting the user by their email
     * @param email
     * @return
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}