package com.eaglebank.api.service;

import com.eaglebank.api.dto.UserDto;
import com.eaglebank.api.exceptiom.BadRequestException;
import com.eaglebank.api.exceptiom.ConflictException;
import com.eaglebank.api.exceptiom.ForbiddenException;
import com.eaglebank.api.exceptiom.ResourceNotFoundException;
import com.eaglebank.api.model.User;
import com.eaglebank.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor // Lombok for constructor injection
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Inject BCryptPasswordEncoder

    @Transactional
    public User createUser(UserDto userDto) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new BadRequestException("User with this username already exists.");
        }
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setEmail(userDto.getEmail());
        return userRepository.save(user);
    }

    public User getUserById(Long userId, String authenticatedUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (!user.getUsername().equals(authenticatedUsername)) {
            throw new ForbiddenException("You are not authorized to view this user's details.");
        }
        return user;
    }

    /**
     * Updates user details by ID, ensuring the requesting user is authorized to update them.
     * Only provided fields in the DTO will be updated.
     *
     * @param userId The ID of the user to update.
     * @param authenticatedUserId The ID of the currently authenticated user.
     * @param userDto Data Transfer Object containing updated user details.
     * @return The updated User entity.
     * @throws ResourceNotFoundException if the user does not exist.
     * @throws ConflictException if the authenticated user is not authorized to update the user's details.
     */
    @Transactional
    public User updateUser(Long userId, Long authenticatedUserId, UserDto userDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Authorization check: A user can only update their own details
        if (!userId.equals(authenticatedUserId)) {
            throw new ConflictException("You are not authorized to update this user's details.");
        }

        // Apply updates if fields are provided in the DTO
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {
            // Encode the new password before updating
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        // Add more fields here if they are updatable (e.g., name, address)

        return userRepository.save(user);
    }

    /**
     * Deletes a user by ID, ensuring the requesting user is authorized and has no associated bank accounts.
     *
     * @param userId The ID of the user to delete.
     * @param authenticatedUserId The ID of the currently authenticated user.
     * @throws ResourceNotFoundException if the user does not exist.
     * @throws ConflictException if the authenticated user is not authorized to delete the user.
     * @throws ConflictException if the user has associated bank accounts.
     */
    @Transactional
    public void deleteUser(Long userId, Long authenticatedUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Authorization check: A user can only delete their own details
        if (!userId.equals(authenticatedUserId)) {
            throw new ConflictException("You are not authorized to delete this user.");
        }

        // Scenario: User wants to delete their user details and they have a bank account
        // If the user has any accounts, prevent deletion
        if (user.getAccounts() != null && !user.getAccounts().isEmpty()) {
            throw new ConflictException("User cannot be deleted as they have associated bank accounts.");
        }

        userRepository.delete(user);
    }

    // Implement update and delete methods following the scenarios.
    // For delete, check if the user has bank accounts (Conflict scenario).
}