package com.eaglebank.api.controller;

import com.eaglebank.api.dto.UserDto;
import com.eaglebank.api.model.User;
import com.eaglebank.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody UserDto userDto) {
        User createdUser = userService.createUser(userDto);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId, Principal principal) {
        // Get authenticated user ID from Principal or SecurityContextHolder
        String authenticatedUserId = principal.getName(); // Assuming username is the user ID

        User user = userService.getUserById(userId, authenticatedUserId);
        return ResponseEntity.ok(user);
    }

    // Implement PATCH for update, DELETE for delete following scenarios.
    // Use @PreAuthorize or custom logic for authorization checks.
}