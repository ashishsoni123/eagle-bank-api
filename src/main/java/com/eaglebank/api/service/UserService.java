package com.eaglebank.api.service;

import com.eaglebank.api.dto.UserRequest;
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

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(UserRequest request) {
        String internalUsername = request.getEmail();

        if (userRepository.existsByUsername(internalUsername)) {
            throw new BadRequestException("User with this email already exists.");
        }

        User user = new User();
        user.setId("usr-" + UUID.randomUUID().toString().substring(0, 8));
        user.setUsername(internalUsername);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setAddress(request.getAddress());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());

        return userRepository.save(user);
    }

    public User getUserById(String userId, String authenticatedUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (!userId.equals(authenticatedUserId)) {
            throw new ForbiddenException("The user is not allowed to access the user details.");
        }
        return user;
    }

    @Transactional
    public User updateUser(String userId, String authenticatedUserId, UserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (!userId.equals(authenticatedUserId)) {
            throw new ForbiddenException("The user is not allowed to update the user details.");
        }

        String internalUsername = request.getEmail();

        if (userRepository.existsByUsername(internalUsername)) {
            throw new BadRequestException("User with this email already exists.");
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
            user.setUsername(request.getEmail());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String userId, String authenticatedUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (!userId.equals(authenticatedUserId)) {
            throw new ForbiddenException("The user is not allowed to delete the user details.");
        }

        if (user.getAccounts() != null && !user.getAccounts().isEmpty()) {
            throw new ConflictException("A user cannot be deleted when they are associated with a bank account.");
        }

        userRepository.delete(user);
    }
}
