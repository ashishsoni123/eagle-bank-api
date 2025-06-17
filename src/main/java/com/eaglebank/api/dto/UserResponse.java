package com.eaglebank.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id; // Matches pattern: ^usr-[A-Za-z0-9]+$
    private String name;
    private Address address;
    private String phoneNumber;
    private String email;
    private LocalDateTime createdTimestamp;
    private LocalDateTime updatedTimestamp;
}
