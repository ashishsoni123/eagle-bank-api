package com.eaglebank.api.service;

import com.eaglebank.api.dto.CreateUserRequest;
import com.eaglebank.api.dto.UpdateUserRequest;
import com.eaglebank.api.dto.Address;
import com.eaglebank.api.exceptiom.BadRequestException;
import com.eaglebank.api.exceptiom.ConflictException;
import com.eaglebank.api.exceptiom.ForbiddenException;
import com.eaglebank.api.exceptiom.ResourceNotFoundException;
import com.eaglebank.api.model.Account;
import com.eaglebank.api.model.User;
import com.eaglebank.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private CreateUserRequest createUserRequest;
    private UpdateUserRequest updateUserRequest;
    private String userId;
    private String hashedPassword;

    @BeforeEach
    void setUp() {
        userId = "usr-12345678";
        hashedPassword = "hashedPassword123";

        testUser = new User();
        testUser.setId(userId);
        testUser.setUsername("test@example.com");
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setPassword(hashedPassword);
        testUser.setPhoneNumber("+44123456789");
        testUser.setAccounts(new ArrayList<>());

        createUserRequest = new CreateUserRequest();
        createUserRequest.setEmail("test@example.com");
        createUserRequest.setPassword("password123");
        createUserRequest.setName("Test User");
        createUserRequest.setPhoneNumber("+44123456789");

        Address address = new Address();
        address.setLine1("123 Test St");
        address.setTown("Test City");
        address.setPostcode("12345");
        createUserRequest.setAddress(address);

        updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setName("Updated Name");
        updateUserRequest.setEmail("updated@example.com");
        updateUserRequest.setPhoneNumber("+44987654321");
    }

    @Test
    void createUser_Success() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.createUser(createUserRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.getId().startsWith("usr-"));
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(hashedPassword, result.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_EmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> userService.createUser(createUserRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_Success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserById(userId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());
    }

    @Test
    void getUserById_NotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserById(userId, userId));
    }

    @Test
    void getUserById_Forbidden() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        String differentUserId = "usr-87654321";

        // Act & Assert
        assertThrows(ForbiddenException.class,
                () -> userService.getUserById(userId, differentUserId));
    }

    @Test
    void updateUser_Success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser(userId, userId, updateUserRequest);

        // Assert
        assertNotNull(result);
        assertEquals(updateUserRequest.getName(), result.getName());
        assertEquals(updateUserRequest.getEmail(), result.getEmail());
        assertEquals(updateUserRequest.getPhoneNumber(), result.getPhoneNumber());
    }

    @Test
    void updateUser_NotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUser(userId, userId, updateUserRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_Forbidden() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        String differentUserId = "usr-87654321";

        // Act & Assert
        assertThrows(ForbiddenException.class,
                () -> userService.updateUser(userId, differentUserId, updateUserRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_Success() {
        // Arrange
        testUser.setAccounts(Collections.emptyList());
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        userService.deleteUser(userId, userId);

        // Assert
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUser_WithAccounts() {
        // Arrange
        testUser.setAccounts(Collections.singletonList(new Account()));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(ConflictException.class,
                () -> userService.deleteUser(userId, userId));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_Forbidden() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        String differentUserId = "usr-87654321";

        // Act & Assert
        assertThrows(ForbiddenException.class,
                () -> userService.deleteUser(userId, differentUserId));
        verify(userRepository, never()).delete(any());
    }
}
