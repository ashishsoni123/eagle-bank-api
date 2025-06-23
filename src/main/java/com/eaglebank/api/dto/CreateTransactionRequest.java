package com.eaglebank.api.dto;

import com.eaglebank.api.enums.Currency;
import com.eaglebank.api.enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateTransactionRequest {
    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Amount must have up to 2 decimal places")
    private BigDecimal amount;

    @NotNull(message = "Currency cannot be null")
    private Currency currency;

    @NotNull(message = "Transaction type cannot be null")
    private TransactionType type;

    private String reference;
}
