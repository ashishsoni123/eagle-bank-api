package com.eaglebank.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Username cannot be blank") // This maps to User's 'email' now
    private String username;

    @NotBlank(message = "Password cannot be blank")
    private String password;
}
