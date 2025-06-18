package com.eaglebank.api.security;

import com.eaglebank.api.exceptiom.ForbiddenException;
import com.eaglebank.api.model.User;
import com.eaglebank.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User testUser;
    private static final String TEST_USERNAME = "test@example.com";
    private static final String TEST_PASSWORD = "hashedPassword123";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername(TEST_USERNAME);
        testUser.setPassword(TEST_PASSWORD);
        testUser.setEmail(TEST_USERNAME);
    }

    @Test
    void loadUserByUsername_WithValidUsername_ShouldReturnUserDetails() {
        // Arrange
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(TEST_USERNAME);

        // Assert
        assertNotNull(userDetails);
        assertEquals(TEST_USERNAME, userDetails.getUsername());
        assertEquals(TEST_PASSWORD, userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().isEmpty());
    }

    @Test
    void loadUserByUsername_WithInvalidUsername_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ForbiddenException.class, () ->
            userDetailsService.loadUserByUsername(TEST_USERNAME)
        );
    }
}
