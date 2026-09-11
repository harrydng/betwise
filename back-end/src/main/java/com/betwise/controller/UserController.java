package com.betwise.controller;

import com.betwise.dto.CreateUserRequest;
import com.betwise.dto.UpdatePasswordRequest;
import com.betwise.dto.UpdateUserRequest;
import com.betwise.dto.UserResponse;
import com.betwise.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * API to listen to get the User for the front-end
     * 
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable Long id) {

        UserResponse user = userService.getUser(id);

        return ResponseEntity.ok(user);
    }

    /**
     * API to listen to update the user given the information fron the front
     * 
     * @param id
     * @param request
     * @return
     */
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request) {

        UserResponse user = userService.updateUser(id, request);

        return ResponseEntity.ok(user);
    }

    /**
     * API to listen to update and hash the password from the front-end
     * 
     * @param id
     * @param request
     * @return
     */
    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(
            @PathVariable Long id,
            @RequestBody UpdatePasswordRequest request) {

        userService.updatePassword(id, request);

        return ResponseEntity.noContent().build();
    }

    /**
     * Deleting the Account
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id) {

        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }
}