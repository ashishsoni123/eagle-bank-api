package com.eaglebank.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private String id; // Matches pattern: ^tan-[A-Za-z0-9]$
    private BigDecimal amount;
    private String currency;
    private String type;
    private String reference;
    private String userId;
    private LocalDateTime createdTimestamp;
}
