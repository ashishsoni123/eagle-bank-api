package com.eaglebank.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionRequest {
    @NotNull
    private BigDecimal amount;
    @NotBlank
    @Pattern(regexp = "DEPOSIT|WITHDRAWAL")
    private String type;
}