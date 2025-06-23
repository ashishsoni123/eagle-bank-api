package com.eaglebank.api.model;

import com.eaglebank.api.enums.Currency;
import com.eaglebank.api.enums.TransactionType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @Pattern(regexp = "^tan-[A-Za-z0-9]+$", message = "Transaction ID must match pattern 'tan-[A-Za-z0-9]+'")
    private String id;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Amount must have up to 2 decimal places")
    private BigDecimal amount;

    @NotNull(message = "Currency cannot be null")
    private Currency currency;

    @NotNull(message = "Transaction type cannot be null")
    private TransactionType type;

    private String reference;

    @NotBlank(message = "User ID cannot be blank for transaction")
    private String userId;

    @CreationTimestamp
    private LocalDateTime createdTimestamp;

    @ManyToOne
    @JoinColumn(name = "account_number", referencedColumnName = "accountNumber")
    private Account account;
}
