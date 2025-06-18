package com.eaglebank.api.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String SECRET = "Y8ZGcPNrOl7XjjFeuGo9OxZAnTMIZVh/Xg7EhUE7yaifca1G7h3jfHAwel/006KoO34TMOLOzKDj5N8d5b/GRA==";
    private static final long EXPIRATION = 3600000; // 1 hour in milliseconds
    private static final String TEST_USERNAME = "test@example.com";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION);
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        // Act
        String token = jwtUtil.generateToken(TEST_USERNAME);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(jwtUtil.validateToken(token));
        assertEquals(TEST_USERNAME, jwtUtil.extractUsername(token));
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Arrange
        String token = jwtUtil.generateToken(TEST_USERNAME);

        // Act & Assert
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        // Arrange
        ReflectionTestUtils.setField(jwtUtil, "expiration", -3600000); // expired 1 hour ago
        String token = jwtUtil.generateToken(TEST_USERNAME);

        // Act & Assert
        assertFalse(jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Act & Assert
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
    }

    @Test
    void validateToken_WithModifiedToken_ShouldReturnFalse() {
        // Arrange
        String token = jwtUtil.generateToken(TEST_USERNAME);
        String modifiedToken = token.substring(0, token.length() - 1) + "X";

        // Act & Assert
        assertFalse(jwtUtil.validateToken(modifiedToken));
    }

    @Test
    void extractUsername_WithValidToken_ShouldReturnUsername() {
        // Arrange
        String token = jwtUtil.generateToken(TEST_USERNAME);

        // Act
        String extractedUsername = jwtUtil.extractUsername(token);

        // Assert
        assertEquals(TEST_USERNAME, extractedUsername);
    }

    @Test
    void extractUsername_WithInvalidToken_ShouldThrowException() {
        // Act & Assert
        assertThrows(MalformedJwtException.class, () ->
            jwtUtil.extractUsername("invalid.token.here")
        );
    }

    @Test
    void extractUsername_WithExpiredToken_ShouldThrowException() {
        // Arrange
        ReflectionTestUtils.setField(jwtUtil, "expiration", -3600000); // expired 1 hour ago
        String token = jwtUtil.generateToken(TEST_USERNAME);

        // Act & Assert
        assertThrows(ExpiredJwtException.class, () ->
            jwtUtil.extractUsername(token)
        );
    }
}
