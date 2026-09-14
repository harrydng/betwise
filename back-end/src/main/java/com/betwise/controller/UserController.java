package com.betwise.controller;

import com.betwise.dto.CreateUserRequest;
import com.betwise.dto.UpdatePasswordRequest;
import com.betwise.dto.UpdateUserRequest;
import com.betwise.dto.UserResponse;
import com.betwise.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.betwise.model.User;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * API to listen to create the user API from the front-end
     * 
     * @param request
     * @return
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @RequestBody CreateUserRequest request) {

        UserResponse user = userService.createUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(user);
    }

    // /**
    //  * API to listen to get the User for the front-end
    //  * 
    //  * @param id
    //  * @return
    //  */
    // @GetMapping("/{id}")
    // public ResponseEntity<UserResponse> getUser(
    //         @PathVariable Long id) {

    //     UserResponse user = userService.getUser(id);

    //     return ResponseEntity.ok(user);
    // }

    /**
     * API to listen to update the user given the information fron the front
     * 
     * @param id
     * @param request
     * @return
     */
    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            Authentication authentication,
            @RequestBody UpdateUserRequest request) {

        User user = (User) authentication.getPrincipal();

        UserResponse response = userService.updateUser(user.getId(), request);

        return ResponseEntity.ok(response);
    }

    /**
     * API to listen to update and hash the password from the front-end
     * 
     * @param id
     * @param request
     * @return
     */
    @PatchMapping("/me/password")
    public ResponseEntity<Void> updateCurrentUserPassword(
            Authentication authentication,
            @RequestBody UpdatePasswordRequest request) {

        User user = (User) authentication.getPrincipal();

        userService.updatePassword(
                user.getId(),
                request);

        return ResponseEntity.noContent().build();
    }

    /**
     * Deleting the Account
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser(
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        userService.deleteUser(user.getId());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                new UserResponse(user));
    }
}