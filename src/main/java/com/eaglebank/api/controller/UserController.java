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
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.eaglebank.api.security.JwtRequestFilter.AUTHENTICATED_USER_ID;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        User createdUser = userService.createUser(request);
        UserResponse response = new UserResponse();
        BeanUtils.copyProperties(createdUser, response);
        return response;
    }

    @GetMapping("/{userId}")
    public UserResponse fetchUserByID(@PathVariable String userId, HttpServletRequest request) {
        String authenticatedUserId = (String) request.getAttribute(AUTHENTICATED_USER_ID);

        User user = userService.getUserById(userId, authenticatedUserId);
        UserResponse response = new UserResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }

    @PatchMapping("/{userId}")
    public UserResponse updateUserByID(
            @PathVariable String userId,
            HttpServletRequest request,
            @Valid @RequestBody UpdateUserRequest updateUserRequest) {

        String authenticatedUserId = (String) request.getAttribute(AUTHENTICATED_USER_ID);
        User updatedUser = userService.updateUser(userId, authenticatedUserId, updateUserRequest);
        UserResponse response = new UserResponse();
        BeanUtils.copyProperties(updatedUser, response);
        return response;
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserByID(@PathVariable String userId, HttpServletRequest request) {

        String authenticatedUserId = (String) request.getAttribute(AUTHENTICATED_USER_ID);
        userService.deleteUser(userId, authenticatedUserId);
    }
}
