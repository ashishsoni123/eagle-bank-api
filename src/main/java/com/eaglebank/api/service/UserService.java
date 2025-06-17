package com.eaglebank.api.service;

import com.eaglebank.api.dto.UserDto;
import com.eaglebank.api.exceptiom.BadRequestException;
import com.eaglebank.api.exceptiom.ForbiddenException;
import com.eaglebank.api.exceptiom.ResourceNotFoundException;
import com.eaglebank.api.model.User;
import com.eaglebank.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor // Lombok for constructor injection
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Inject BCryptPasswordEncoder

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

    public User getUserById(Long userId, Long authenticatedUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (!userId.equals(authenticatedUserId)) {
            throw new ForbiddenException("You are not authorized to view this user's details.");
        }
        return user;
    }

    // Implement update and delete methods following the scenarios.
    // For delete, check if the user has bank accounts (Conflict scenario).
}