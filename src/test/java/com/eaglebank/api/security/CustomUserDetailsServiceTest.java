package com.eaglebank.api.security;

import com.eaglebank.api.model.User;
import com.eaglebank.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Tests")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("test@example.com");
        testUser.setPassword("encodedPassword");
    }

    @Test
    @DisplayName("Should successfully load user by username")
    void loadUserByUsername_Success() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getPassword(), result.getPassword());
        assertTrue(result.getAuthorities().isEmpty());

        // Verify
        verify(userRepository).findByUsername("test@example.com");
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user not found")
    void loadUserByUsername_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(UsernameNotFoundException.class, () ->
            userDetailsService.loadUserByUsername("nonexistent@example.com")
        );

        assertEquals("User not found with username: nonexistent@example.com", exception.getMessage());

        // Verify
        verify(userRepository).findByUsername("nonexistent@example.com");
    }

    @Test
    @DisplayName("Should handle null username")
    void loadUserByUsername_NullUsername() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            userDetailsService.loadUserByUsername(null)
        );

        // Verify
        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    @DisplayName("Should handle empty username")
    void loadUserByUsername_EmptyUsername() {
        // Arrange
        when(userRepository.findByUsername("")).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(UsernameNotFoundException.class, () ->
            userDetailsService.loadUserByUsername("")
        );

        assertEquals("User not found with username: ", exception.getMessage());

        // Verify
        verify(userRepository).findByUsername("");
    }

    @Test
    @DisplayName("Should properly map user authorities")
    void loadUserByUsername_WithAuthorities() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername("test@example.com");

        // Assert
        assertNotNull(result);
        assertTrue(result.getAuthorities().isEmpty()); // Currently no authorities are added

        // Verify
        verify(userRepository).findByUsername("test@example.com");
    }
}
