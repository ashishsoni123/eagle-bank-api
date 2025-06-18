package com.eaglebank.api.service;

import com.eaglebank.api.exceptiom.ForbiddenException;
import com.eaglebank.api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public String authenticateAndGenerateToken(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            return jwtUtil.generateToken(authentication.getName());
        } catch (AuthenticationException e) {
            throw new ForbiddenException("Invalid username or password");
        }
    }
}
