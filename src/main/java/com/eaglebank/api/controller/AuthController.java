package com.eaglebank.api.controller;

import com.eaglebank.api.dto.AuthResponse;
import com.eaglebank.api.dto.LoginRequest;
import com.eaglebank.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return new AuthResponse(authService.authenticateAndGenerateToken(loginRequest.getUsername(), loginRequest.getPassword()));
    }
}
