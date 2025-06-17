package com.eaglebank.api.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT Request Filter Tests")
class JwtRequestFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtRequestFilter jwtRequestFilter;

    private UserDetails userDetails;
    private String token;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        userDetails = new User("test@example.com", "password", new ArrayList<>());
        token = "Bearer valid.jwt.token";
    }

    @Test
    @DisplayName("Should successfully authenticate with valid JWT token")
    void doFilterInternal_ValidToken_Success() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtUtil.extractUsername(anyString())).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtUtil.validateToken(anyString())).thenReturn(true);

        // Act
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("test@example.com", SecurityContextHolder.getContext().getAuthentication().getName());

        // Verify
        verify(filterChain).doFilter(request, response);
        verify(userDetailsService).loadUserByUsername("test@example.com");
    }

    @Test
    @DisplayName("Should continue chain without authentication for missing Authorization header")
    void doFilterInternal_NoAuthHeader() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // Verify
        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    @DisplayName("Should handle expired JWT token")
    void doFilterInternal_ExpiredToken() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtUtil.extractUsername(anyString())).thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        // Act
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // Verify
        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    @DisplayName("Should handle invalid token format")
    void doFilterInternal_InvalidTokenFormat() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("InvalidTokenFormat");

        // Act
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // Verify
        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    @DisplayName("Should handle invalid token validation")
    void doFilterInternal_InvalidToken() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtUtil.extractUsername(anyString())).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtUtil.validateToken(anyString())).thenReturn(false);

        // Act
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // Verify
        verify(filterChain).doFilter(request, response);
        verify(userDetailsService).loadUserByUsername("test@example.com");
    }

    @Test
    @DisplayName("Should handle general JWT exception")
    void doFilterInternal_JwtException() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtUtil.extractUsername(anyString())).thenThrow(new RuntimeException("JWT Error"));

        // Act
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // Verify
        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }
}
