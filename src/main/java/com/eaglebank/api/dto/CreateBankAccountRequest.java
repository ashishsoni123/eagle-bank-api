package com.eaglebank.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateBankAccountRequest {
    @NotBlank(message = "Account name cannot be blank")
    private String name;

    @NotBlank(message = "Account type cannot be blank")
    @Pattern(regexp = "personal", message = "Account type must be 'personal'")
    private String accountType;
}
