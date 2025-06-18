package com.eaglebank.api.service;

import com.eaglebank.api.exceptiom.ForbiddenException;
import com.eaglebank.api.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Mock
    private Authentication authentication;

    private String username;
    private String password;
    private String token;

    @BeforeEach
    void setUp() {
        username = "test@example.com";
        password = "password123";
        token = "test.jwt.token";
    }

    @Test
    void authenticateAndGenerateToken_Success() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        when(jwtUtil.generateToken(username)).thenReturn(token);

        // Act
        String result = authService.authenticateAndGenerateToken(username, password);

        // Assert
        assertNotNull(result);
        assertEquals(token, result);
    }

    @Test
    void authenticateAndGenerateToken_InvalidCredentials() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThrows(ForbiddenException.class,
                () -> authService.authenticateAndGenerateToken(username, password));
    }
}
