package com.eaglebank.api.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateBankAccountRequest {
    private String name;
    @Pattern(regexp = "personal", message = "Account type must be 'personal'")
    private String accountType;
}
