package com.eaglebank.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountResponse {
    private String accountNumber; // Matches pattern: ^01\d{6}$
    private String sortCode; // Matches enum: "10-10-10"
    private String name;
    private String accountType; // Matches enum: "personal"
    private BigDecimal balance;
    private String currency; // Matches enum: "GBP"
    private LocalDateTime createdTimestamp;
    private LocalDateTime updatedTimestamp;
}
