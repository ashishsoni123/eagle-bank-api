package com.eaglebank.api.security;

import com.eaglebank.api.exceptiom.ForbiddenException;
import com.eaglebank.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch the user from your database using UserRepository
        com.eaglebank.api.model.User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ForbiddenException("User not found with username: " + username));

        // Build and return a Spring Security User object
        // The password should be the BCrypt encoded password from the database
        // Roles/Authorities can be added in the third parameter (e.g., new ArrayList<>() for now,
        // but you would fetch user roles from the DB if applicable)
        return new User(user.getUsername(), user.getPassword(), new ArrayList<>());
    }
}