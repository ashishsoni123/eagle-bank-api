package com.eaglebank.api.service;

import com.eaglebank.api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public String authenticateAndGenerateToken(String username, String password) {
        try {
            // Authenticate the user using Spring Security's AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // If authentication is successful, generate a JWT token
            // The username from the authenticated principal is used as the subject for the JWT
            return jwtUtil.generateToken(authentication.getName());
        } catch (AuthenticationException e) {
            // Handle specific authentication exceptions (e.g., BadCredentialsException)
            // You can throw a custom exception or return a specific error message
            throw new RuntimeException("Invalid username or password", e); // Or a more specific custom exception
        }
    }
}