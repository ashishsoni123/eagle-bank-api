package com.eaglebank.api.dto;

import com.eaglebank.api.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private String accountNumber; // Matches pattern: ^01\d{6}$
    private String sortCode; // Matches enum: "10-10-10"
    private String name;
    private AccountType accountType;
    private BigDecimal balance;
    private String currency; // Matches enum: "GBP"
    private LocalDateTime createdTimestamp;
    private LocalDateTime updatedTimestamp;
}
