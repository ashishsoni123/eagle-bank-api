package com.eaglebank.api.service;


import com.eaglebank.api.dto.UserDto;
import com.eaglebank.api.exceptiom.BadRequestException;
import com.eaglebank.api.exceptiom.ConflictException;
import com.eaglebank.api.exceptiom.ForbiddenException;
import com.eaglebank.api.exceptiom.ResourceNotFoundException;
import com.eaglebank.api.model.Account;
import com.eaglebank.api.model.User;
import com.eaglebank.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setAccounts(new ArrayList<>()); // Initialize accounts list

        testUserDto = new UserDto();
        testUserDto.setUsername("newuser");
        testUserDto.setPassword("plainPassword");
        testUserDto.setEmail("new@example.com");
    }

    @Test
    @DisplayName("createUser: Should create a new user successfully")
    void createUser_Success() {
        when(userRepository.existsByUsername(testUserDto.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(testUserDto.getPassword())).thenReturn("encodedPlainPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser); // Return testUser for simplicity

        User createdUser = userService.createUser(testUserDto);

        assertNotNull(createdUser);
        assertEquals(testUser.getUsername(), createdUser.getUsername());
        assertEquals(testUser.getEmail(), createdUser.getEmail());
        assertEquals("encodedPassword", createdUser.getPassword()); // Verify encoded password
        verify(userRepository, times(1)).existsByUsername(testUserDto.getUsername());
        verify(passwordEncoder, times(1)).encode(testUserDto.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("createUser: Should throw BadRequestException if username already exists")
    void createUser_UsernameAlreadyExists() {
        when(userRepository.existsByUsername(testUserDto.getUsername())).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> userService.createUser(testUserDto));

        assertEquals("User with this username already exists.", exception.getMessage());
        verify(userRepository, times(1)).existsByUsername(testUserDto.getUsername());
        verify(passwordEncoder, never()).encode(anyString()); // Ensure password not encoded
        verify(userRepository, never()).save(any(User.class)); // Ensure user not saved
    }

    @Test
    @DisplayName("getUserById: Should return user details for own user ID")
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User foundUser = userService.getUserById(1L, testUser.getUsername());

        assertNotNull(foundUser);
        assertEquals(testUser.getId(), foundUser.getId());
        assertEquals(testUser.getUsername(), foundUser.getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getUserById: Should throw ResourceNotFoundException if user not found")
    void getUserById_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserById(99L, testUser.getUsername()));

        assertEquals("User not found with ID: 99", exception.getMessage());
        verify(userRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("getUserById: Should throw ForbiddenException if fetching another user's details")
    void getUserById_Forbidden() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser)); // Another user exists

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> userService.getUserById(2L, "test")); // User 1 trying to fetch User 2

        assertEquals("You are not authorized to view this user's details.", exception.getMessage());
        verify(userRepository, times(1)).findById(2L);
    }

    @Test
    @DisplayName("updateUser: Should update user details successfully for own user ID")
    void updateUser_Success() {
        UserDto updateDto = new UserDto();
        updateDto.setEmail("updated@example.com");
        updateDto.setPassword("newPlainPassword"); // Test password update

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPlainPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser); // Return updated user

        User updatedUser = userService.updateUser(1L, 1L, updateDto);

        assertNotNull(updatedUser);
        assertEquals(testUser.getId(), updatedUser.getId());
        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals("newEncodedPassword", updatedUser.getPassword());
        verify(userRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).encode("newPlainPassword");
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("updateUser: Should throw ResourceNotFoundException if user to update not found")
    void updateUser_NotFound() {
        UserDto updateDto = new UserDto();
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUser(99L, 1L, updateDto));

        assertEquals("User not found with ID: 99", exception.getMessage());
        verify(userRepository, times(1)).findById(99L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser: Should throw ForbiddenException if updating another user's details")
    void updateUser_Forbidden() {
        UserDto updateDto = new UserDto();
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User())); // Another user exists

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.updateUser(2L, 1L, updateDto)); // User 1 trying to update User 2

        assertEquals("You are not authorized to update this user's details.", exception.getMessage());
        verify(userRepository, times(1)).findById(2L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("deleteUser: Should delete user successfully if no bank accounts")
    void deleteUser_Success_NoAccounts() {
        testUser.setAccounts(Collections.emptyList()); // Ensure no accounts
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);

        assertDoesNotThrow(() -> userService.deleteUser(1L, 1L));

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    @DisplayName("deleteUser: Should throw ConflictException if user has bank accounts")
    void deleteUser_HasAccounts_Conflict() {
        testUser.getAccounts().add(new Account()); // Add a dummy account
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.deleteUser(1L, 1L));

        assertEquals("User cannot be deleted as they have associated bank accounts.", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("deleteUser: Should throw ResourceNotFoundException if user to delete not found")
    void deleteUser_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.deleteUser(99L, 1L));

        assertEquals("User not found with ID: 99", exception.getMessage());
        verify(userRepository, times(1)).findById(99L);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("deleteUser: Should throw ForbiddenException if deleting another user's details")
    void deleteUser_Forbidden() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User())); // Another user exists

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.deleteUser(2L, 1L)); // User 1 trying to delete User 2

        assertEquals("You are not authorized to delete this user.", exception.getMessage());
        verify(userRepository, times(1)).findById(2L);
        verify(userRepository, never()).delete(any(User.class));
    }
}
