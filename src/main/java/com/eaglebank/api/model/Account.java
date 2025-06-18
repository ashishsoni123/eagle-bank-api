package com.eaglebank.api.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    @Pattern(regexp = "^01\\d{6}$", message = "Account number must be 01 followed by 6 digits")
    private String accountNumber;

    @NotBlank(message = "Sort code cannot be blank")
    @Pattern(regexp = "^\\d{2}-\\d{2}-\\d{2}$", message = "Sort code must be in format xx-xx-xx where x is a digit")
    private String sortCode;

    @NotBlank(message = "Account name cannot be blank")
    private String name;

    @NotBlank(message = "Account type cannot be blank")
    @Pattern(regexp = "personal", message = "Account type must be 'personal'")
    private String accountType;

    @NotNull(message = "Balance cannot be null")
    @DecimalMin(value = "0.00", message = "Balance cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Balance must have up to 2 decimal places")
    private BigDecimal balance;

    @NotBlank(message = "Currency cannot be blank")
    @Pattern(regexp = "GBP", message = "Currency must be 'GBP'")
    private String currency;

    @CreationTimestamp
    private LocalDateTime createdTimestamp;

    @UpdateTimestamp
    private LocalDateTime updatedTimestamp;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();
}
