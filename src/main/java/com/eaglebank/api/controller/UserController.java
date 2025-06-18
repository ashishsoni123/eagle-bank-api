package com.eaglebank.api.controller;

import com.eaglebank.api.dto.CreateUserRequest;
import com.eaglebank.api.dto.UpdateUserRequest;
import com.eaglebank.api.dto.UserResponse;
import com.eaglebank.api.model.User;
import com.eaglebank.api.repository.UserRepository;
import com.eaglebank.api.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.eaglebank.api.security.JwtRequestFilter.AUTHENTICATED_USER_ID;

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
    public ResponseEntity<UserResponse> fetchUserByID(@PathVariable String userId, HttpServletRequest request) {
        String authenticatedUserId = (String) request.getAttribute(AUTHENTICATED_USER_ID);

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
            HttpServletRequest request,
            @Valid @RequestBody UpdateUserRequest updateUserRequest) {

      
        String authenticatedUserId = (String) request.getAttribute(AUTHENTICATED_USER_ID);
        User updatedUser = userService.updateUser(userId, authenticatedUserId, updateUserRequest);
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
    public ResponseEntity<Void> deleteUserByID(@PathVariable String userId, HttpServletRequest request) {

        String authenticatedUserId = (String) request.getAttribute(AUTHENTICATED_USER_ID);
        userService.deleteUser(userId, authenticatedUserId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
