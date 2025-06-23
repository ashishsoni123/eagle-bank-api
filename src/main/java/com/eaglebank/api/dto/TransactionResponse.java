package com.eaglebank.api.dto;

import com.eaglebank.api.enums.Currency;
import com.eaglebank.api.enums.TransactionType;
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
    private Currency currency;
    private TransactionType type;
    private String reference;
    private String userId;
    private LocalDateTime createdTimestamp;
}
