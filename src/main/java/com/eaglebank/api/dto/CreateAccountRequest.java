package com.eaglebank.api.dto;

import com.eaglebank.api.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class CreateAccountRequest {
    @NotBlank(message = "Account name cannot be blank")
    @Length(max = 100)
    private String name;

    @NotNull(message = "Account type cannot be blank")
    private AccountType accountType;
}
