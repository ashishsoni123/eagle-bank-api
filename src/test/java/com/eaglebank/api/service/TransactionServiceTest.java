package com.eaglebank.api.service;

import com.eaglebank.api.dto.CreateTransactionRequest;
import com.eaglebank.api.exceptiom.InsufficientFundsException;
import com.eaglebank.api.exceptiom.ResourceNotFoundException;
import com.eaglebank.api.model.Account;
import com.eaglebank.api.model.Transaction;
import com.eaglebank.api.repository.AccountRepository;
import com.eaglebank.api.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.eaglebank.api.enums.TransactionType.DEPOSIT;
import static com.eaglebank.api.enums.TransactionType.WITHDRAWAL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Account testAccount;
    private Transaction testTransaction;
    private CreateTransactionRequest depositRequest;
    private CreateTransactionRequest withdrawalRequest;
    private String accountNumber;
    private String userId;
    private String transactionId;

    @BeforeEach
    void setUp() {
        accountNumber = "1234567890";
        userId = "usr-12345678";
        transactionId = "tan-12345678";

        testAccount = new Account();
        testAccount.setAccountNumber(accountNumber);
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setCurrency("GBP");

        testTransaction = new Transaction();
        testTransaction.setId(transactionId);
        testTransaction.setAmount(new BigDecimal("100.00"));
        testTransaction.setCurrency("GBP");
        testTransaction.setType(DEPOSIT);
        testTransaction.setReference("Test transaction");
        testTransaction.setUserId(userId);
        testTransaction.setCreatedTimestamp(LocalDateTime.now());
        testTransaction.setAccount(testAccount);

        depositRequest = new CreateTransactionRequest();
        depositRequest.setAmount(new BigDecimal("100.00"));
        depositRequest.setCurrency("GBP");
        depositRequest.setType(DEPOSIT);
        depositRequest.setReference("Test deposit");

        withdrawalRequest = new CreateTransactionRequest();
        withdrawalRequest.setAmount(new BigDecimal("50.00"));
        withdrawalRequest.setCurrency("GBP");
        withdrawalRequest.setType(WITHDRAWAL);
        withdrawalRequest.setReference("Test withdrawal");
    }

    @Test
    void performTransaction_SuccessfulDeposit() {
        // Arrange
        when(accountRepository.findByAccountNumberAndUserId(accountNumber, userId))
                .thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // Act
        Transaction result = transactionService.performTransaction(accountNumber, userId, depositRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testTransaction.getAmount(), result.getAmount());
        assertEquals(testTransaction.getType(), result.getType());
        verify(accountRepository).save(testAccount);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void performTransaction_SuccessfulWithdrawal() {
        // Arrange
        when(accountRepository.findByAccountNumberAndUserId(accountNumber, userId))
                .thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // Act
        Transaction result = transactionService.performTransaction(accountNumber, userId, withdrawalRequest);

        // Assert
        assertNotNull(result);
        verify(accountRepository).save(testAccount);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void performTransaction_InsufficientFunds() {
        // Arrange
        withdrawalRequest.setAmount(new BigDecimal("2000.00")); // More than account balance
        when(accountRepository.findByAccountNumberAndUserId(accountNumber, userId))
                .thenReturn(Optional.of(testAccount));

        // Act & Assert
        assertThrows(InsufficientFundsException.class,
                () -> transactionService.performTransaction(accountNumber, userId, withdrawalRequest));
        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void performTransaction_AccountNotFound() {
        // Arrange
        when(accountRepository.findByAccountNumberAndUserId(accountNumber, userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.performTransaction(accountNumber, userId, depositRequest));
        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void getTransactionsByAccountNumber_Success() {
        // Arrange
        List<Transaction> transactions = Arrays.asList(testTransaction);
        when(accountRepository.findByAccountNumberAndUserId(accountNumber, userId))
                .thenReturn(Optional.of(testAccount));
        when(transactionRepository.findByAccountAccountNumber(accountNumber))
                .thenReturn(transactions);

        // Act
        List<Transaction> result = transactionService.getTransactionsByAccountNumber(accountNumber, userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTransaction.getId(), result.get(0).getId());
    }

    @Test
    void getTransactionsByAccountNumber_AccountNotFound() {
        // Arrange
        when(accountRepository.findByAccountNumberAndUserId(accountNumber, userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.getTransactionsByAccountNumber(accountNumber, userId));
        verify(transactionRepository, never()).findByAccountAccountNumber(any());
    }

    @Test
    void getTransactionByIdAndAccountNumber_Success() {
        // Arrange
        when(accountRepository.findByAccountNumberAndUserId(accountNumber, userId))
                .thenReturn(Optional.of(testAccount));
        when(transactionRepository.findByIdAndAccountAccountNumber(transactionId, accountNumber))
                .thenReturn(Optional.of(testTransaction));

        // Act
        Transaction result = transactionService.getTransactionByIdAndAccountNumber(transactionId, accountNumber, userId);

        // Assert
        assertNotNull(result);
        assertEquals(testTransaction.getId(), result.getId());
        assertEquals(testTransaction.getAmount(), result.getAmount());
    }

    @Test
    void getTransactionByIdAndAccountNumber_TransactionNotFound() {
        // Arrange
        when(accountRepository.findByAccountNumberAndUserId(accountNumber, userId))
                .thenReturn(Optional.of(testAccount));
        when(transactionRepository.findByIdAndAccountAccountNumber(transactionId, accountNumber))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.getTransactionByIdAndAccountNumber(transactionId, accountNumber, userId));
    }
}
