package com.eaglebank.api.service;

import com.eaglebank.api.dto.CreateUserRequest;
import com.eaglebank.api.dto.UpdateUserRequest;
import com.eaglebank.api.exceptiom.BadRequestException;
import com.eaglebank.api.exceptiom.ConflictException;
import com.eaglebank.api.exceptiom.ForbiddenException;
import com.eaglebank.api.exceptiom.ResourceNotFoundException;
import com.eaglebank.api.model.User;
import com.eaglebank.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
    public User createUser(CreateUserRequest request) {
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

        if (!userId.equalsIgnoreCase(authenticatedUserId)) {
            throw new ForbiddenException("The user is not allowed to access the user details.");
        }
        return user;
    }

    @Transactional
    public User updateUser(String userId, String authenticatedUserId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (!userId.equalsIgnoreCase(authenticatedUserId)) {
            throw new ForbiddenException("The user is not allowed to update the user details.");
        }

        String internalUsername = request.getEmail();

        if (userRepository.existsByUsername(internalUsername)) {
            throw new BadRequestException("User with this email already exists.");
        }

        if (StringUtils.isNotBlank(request.getName())) {
            user.setName(request.getName());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (StringUtils.isNotBlank(request.getPhoneNumber())) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (StringUtils.isNotBlank(request.getEmail())) {
            user.setEmail(request.getEmail());
            user.setUsername(request.getEmail());
        }
        if (StringUtils.isNotBlank(request.getPassword())) {
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
