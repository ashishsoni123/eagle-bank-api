package com.eaglebank.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountDto {
    // You might not need this if account number is generated, or make it optional.
    // private String accountNumber;
    private BigDecimal initialBalance;
}
