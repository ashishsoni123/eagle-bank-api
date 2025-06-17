package com.eaglebank.api.controller;

import com.eaglebank.api.dto.CreateUserRequest;
import com.eaglebank.api.dto.UpdateUserRequest;
import com.eaglebank.api.dto.UserResponse;
import com.eaglebank.api.model.User;
import com.eaglebank.api.repository.UserRepository;
import com.eaglebank.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User createdUser = userService.createUser(request);
        UserResponse response = new UserResponse(
                createdUser.getId(),
                createdUser.getName(),
                createdUser.getAddress(),
                createdUser.getPhoneNumber(),
                createdUser.getEmail(),
                createdUser.getCreatedTimestamp(),
                createdUser.getUpdatedTimestamp()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> fetchUserByID(@PathVariable String userId, Principal principal) {
        Optional<User> authenticatedUser = userRepository.findByUsername(principal.getName());
        String authenticatedUserId = authenticatedUser.map(User::getId)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in DB. This should not happen."));

        User user = userService.getUserById(userId, authenticatedUserId);
        UserResponse response = new UserResponse(
                user.getId(),
                user.getName(),
                user.getAddress(),
                user.getPhoneNumber(),
                user.getEmail(),
                user.getCreatedTimestamp(),
                user.getUpdatedTimestamp()
        );
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUserByID(
            @PathVariable String userId,
            Principal principal,
            @Valid @RequestBody UpdateUserRequest request) {

        Optional<User> authenticatedUser = userRepository.findByUsername(principal.getName());
        String authenticatedUserId = authenticatedUser.map(User::getId)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in DB. This should not happen."));

        User updatedUser = userService.updateUser(userId, authenticatedUserId, request);
        UserResponse response = new UserResponse(
                updatedUser.getId(),
                updatedUser.getName(),
                updatedUser.getAddress(),
                updatedUser.getPhoneNumber(),
                updatedUser.getEmail(),
                updatedUser.getCreatedTimestamp(),
                updatedUser.getUpdatedTimestamp()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUserByID(@PathVariable String userId, Principal principal) {
        Optional<User> authenticatedUser = userRepository.findByUsername(principal.getName());
        String authenticatedUserId = authenticatedUser.map(User::getId)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in DB. This should not happen."));

        userService.deleteUser(userId, authenticatedUserId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
