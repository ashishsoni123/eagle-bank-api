package com.eaglebank.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String name;

    @Valid
    private Address address;

    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Phone number must be in E.164 format (e.g., +441234567890)")
    private String phoneNumber;

    @Email(message = "Email should be valid")
    private String email;

    private String password;
}
