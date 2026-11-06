package com.betwise.service;

import com.betwise.exception.ConflictException;
import com.betwise.exception.ResourceNotFoundException;
import com.betwise.dto.CreateUserRequest;
import com.betwise.dto.UpdatePasswordRequest;
import com.betwise.dto.UpdateUserRequest;
import com.betwise.dto.UserResponse;
import com.betwise.model.User;
import com.betwise.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountCleanupService accountCleanupService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AccountCleanupService accountCleanupService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountCleanupService = accountCleanupService;
    }

    /**
     * Creating a user
     * 
     * @param request
     * @return
     */
    public UserResponse createUser(CreateUserRequest request) {
        String name = validName(request.getName());
        String email = validEmail(request.getEmail());
        validatePassword(request.getPassword());

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email is already in use");
        }

        User user = new User();

        user.setName(name);
        user.setEmail(email);
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
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

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
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getName() != null) {
            user.setName(validName(request.getName()));
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
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Accounts created with Google have no password yet. The user is
        // already signed in, so they can set one without a current password.
        if (user.hasPassword()) {
            boolean matches = request.getCurrentPassword() != null
                    && passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash());

            if (!matches) {
                throw new IllegalArgumentException("Current password is incorrect");
            }
        }

        validatePassword(request.getNewPassword());

        String newHashedPassword = passwordEncoder.encode(request.getNewPassword());

        user.setPasswordHash(newHashedPassword);

        userRepository.save(user);
    }

    /**
     * Deleting user account
     * 
     * @param userId
     */
    @Transactional
    public void deleteUser(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        // Portfolios, watchlists, progress and notifications all reference
        // the user, so remove them first or the foreign keys block the delete.
        accountCleanupService.deleteEverythingOwnedBy(userId);

        userRepository.deleteById(userId);
    }

    /**
     * Getting the user by their email
     * @param email
     * @return
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // MARK: - Validation

    static final int MIN_PASSWORD_LENGTH = 8;

    // BCrypt only uses the first 72 bytes, so anything past that would be
    // silently ignored. Reject it instead.
    static final int MAX_PASSWORD_BYTES = 72;

    private static final java.util.regex.Pattern EMAIL =
            java.util.regex.Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private static String validName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }

        String trimmed = name.trim();

        if (trimmed.length() > 100) {
            throw new IllegalArgumentException("Name must be 100 characters or fewer");
        }

        return trimmed;
    }

    private static String validEmail(String email) {
        String normalized = email == null ? "" : email.toLowerCase().trim();

        if (normalized.length() > 255 || !EMAIL.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Enter a valid email address");
        }

        return normalized;
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }

        if (password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new IllegalArgumentException("Password is too long");
        }

        if (password.isBlank()) {
            throw new IllegalArgumentException("Password can't be only spaces");
        }
    }
}