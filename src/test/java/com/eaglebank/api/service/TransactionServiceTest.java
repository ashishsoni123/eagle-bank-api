package com.eaglebank.api.service;

import com.eaglebank.api.dto.TransactionRequest;
import com.eaglebank.api.exceptiom.BadRequestException;
import com.eaglebank.api.exceptiom.InsufficientFundsException;
import com.eaglebank.api.exceptiom.ResourceNotFoundException;
import com.eaglebank.api.model.Account;
import com.eaglebank.api.model.Transaction;
import com.eaglebank.api.model.User;
import com.eaglebank.api.repository.AccountRepository;
import com.eaglebank.api.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService Unit Tests")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private Account testAccount;
    private Transaction depositTransaction;
    private Transaction withdrawalTransaction;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testAccount = new Account();
        testAccount.setId(101L);
        testAccount.setAccountNumber("ACC123");
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setUser(testUser); // Link account to user

        depositTransaction = new Transaction();
        depositTransaction.setId(1L);
        depositTransaction.setAmount(new BigDecimal("100.00"));
        depositTransaction.setType("DEPOSIT");
        depositTransaction.setTimestamp(LocalDateTime.now());
        depositTransaction.setAccount(testAccount);

        withdrawalTransaction = new Transaction();
        withdrawalTransaction.setId(2L);
        withdrawalTransaction.setAmount(new BigDecimal("50.00"));
        withdrawalTransaction.setType("WITHDRAWAL");
        withdrawalTransaction.setTimestamp(LocalDateTime.now());
        withdrawalTransaction.setAccount(testAccount);
    }

    // --- Perform Transaction Tests ---

    @Test
    @DisplayName("performTransaction: Should successfully deposit money")
    void performTransaction_DepositSuccess() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("200.00"));
        request.setType("DEPOSIT");

        when(accountRepository.findByIdAndUserId(testAccount.getId(), testUser.getId()))
                .thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount); // Mock saving updated account
        when(transactionRepository.save(any(Transaction.class))).thenReturn(depositTransaction); // Mock saving transaction

        Transaction result = transactionService.performTransaction(testAccount.getId(), testUser.getId(), request);

        assertNotNull(result);
        assertEquals(new BigDecimal("1200.00"), testAccount.getBalance()); // Balance updated
        assertEquals("DEPOSIT", result.getType());
        assertEquals(new BigDecimal("200.00"), result.getAmount());

        // Verify that account was saved with updated balance and transaction was saved
        verify(accountRepository, times(1)).save(testAccount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("performTransaction: Should successfully withdraw money with sufficient funds")
    void performTransaction_WithdrawalSuccess() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("200.00"));
        request.setType("WITHDRAWAL");

        when(accountRepository.findByIdAndUserId(testAccount.getId(), testUser.getId()))
                .thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(withdrawalTransaction);

        Transaction result = transactionService.performTransaction(testAccount.getId(), testUser.getId(), request);

        assertNotNull(result);
        assertEquals(new BigDecimal("800.00"), testAccount.getBalance()); // Balance updated
        assertEquals("WITHDRAWAL", result.getType());
        assertEquals(new BigDecimal("200.00"), result.getAmount());

        verify(accountRepository, times(1)).save(testAccount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("performTransaction: Should throw InsufficientFundsException for withdrawal with insufficient funds")
    void performTransaction_WithdrawalInsufficientFunds() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("1500.00")); // More than current balance
        request.setType("WITHDRAWAL");

        when(accountRepository.findByIdAndUserId(testAccount.getId(), testUser.getId()))
                .thenReturn(Optional.of(testAccount));

        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class,
                () -> transactionService.performTransaction(testAccount.getId(), testUser.getId(), request));

        assertEquals("Insufficient funds for withdrawal.", exception.getMessage());
        // Verify no changes were saved
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("performTransaction: Should throw ResourceNotFoundException if account not found for user")
    void performTransaction_AccountNotFound() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setType("DEPOSIT");

        when(accountRepository.findByIdAndUserId(testAccount.getId(), testUser.getId()))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> transactionService.performTransaction(testAccount.getId(), testUser.getId(), request));

        assertEquals("Account not found or not associated with your user.", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("performTransaction: Should throw BadRequestException for invalid transaction type")
    void performTransaction_InvalidTransactionType() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setType("INVALID_TYPE"); // Invalid type

        when(accountRepository.findByIdAndUserId(testAccount.getId(), testUser.getId()))
                .thenReturn(Optional.of(testAccount));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> transactionService.performTransaction(testAccount.getId(), testUser.getId(), request));

        assertEquals("Invalid transaction type: INVALID_TYPE", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    // --- List Transactions Tests ---

    @Test
    @DisplayName("getTransactionsByAccountId: Should return list of transactions for a valid account and user")
    void getTransactionsByAccountId_Success() {
        List<Transaction> transactions = Arrays.asList(depositTransaction, withdrawalTransaction);
        when(accountRepository.findByIdAndUserId(testAccount.getId(), testUser.getId()))
                .thenReturn(Optional.of(testAccount));
        when(transactionRepository.findByAccountId(testAccount.getId())).thenReturn(transactions);

        List<Transaction> result = transactionService.getTransactionsByAccountId(testAccount.getId(), testUser.getId());

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(depositTransaction));
        assertTrue(result.contains(withdrawalTransaction));
        verify(accountRepository, times(1)).findByIdAndUserId(testAccount.getId(), testUser.getId());
        verify(transactionRepository, times(1)).findByAccountId(testAccount.getId());
    }

    @Test
    @DisplayName("getTransactionsByAccountId: Should return empty list if no transactions found")
    void getTransactionsByAccountId_EmptyList() {
        when(accountRepository.findByIdAndUserId(testAccount.getId(), testUser.getId()))
                .thenReturn(Optional.of(testAccount));
        when(transactionRepository.findByAccountId(testAccount.getId())).thenReturn(Collections.emptyList());

        List<Transaction> result = transactionService.getTransactionsByAccountId(testAccount.getId(), testUser.getId());

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(accountRepository, times(1)).findByIdAndUserId(testAccount.getId(), testUser.getId());
        verify(transactionRepository, times(1)).findByAccountId(testAccount.getId());
    }

    @Test
    @DisplayName("getTransactionsByAccountId: Should throw ResourceNotFoundException if account not found for user")
    void getTransactionsByAccountId_AccountNotFound() {
        when(accountRepository.findByIdAndUserId(testAccount.getId(), testUser.getId()))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> transactionService.getTransactionsByAccountId(testAccount.getId(), testUser.getId()));

        assertEquals("Account not found or not associated with your user.", exception.getMessage());
        verify(transactionRepository, never()).findByAccountId(anyLong());
    }

    // --- Fetch Transaction Tests ---

    @Test
    @DisplayName("getTransactionByIdAndAccountId: Should return transaction for valid IDs and user")
    void getTransactionByIdAndAccountId_Success() {
        when(accountRepository.findByIdAndUserId(testAccount.getId(), testUser.getId()))
                .thenReturn(Optional.of(testAccount));
        when(transactionRepository.findByIdAndAccountId(depositTransaction.getId(), testAccount.getId()))
                .thenReturn(Optional.of(depositTransaction));

        Transaction result = transactionService.getTransactionByIdAndAccountId(
                depositTransaction.getId(), testAccount.getId(), testUser.getId());

        assertNotNull(result);
        assertEquals(depositTransaction.getId(), result.getId());
        assertEquals(depositTransaction.getAmount(), result.getAmount());
        verify(accountRepository, times(1)).findByIdAndUserId(testAccount.getId(), testUser.getId());
        verify(transactionRepository, times(1)).findByIdAndAccountId(depositTransaction.getId(), testAccount.getId());
    }

    @Test
    @DisplayName("getTransactionByIdAndAccountId: Should throw ResourceNotFoundException if account not found for user")
    void getTransactionByIdAndAccountId_AccountNotFound() {
        when(accountRepository.findByIdAndUserId(testAccount.getId(), testUser.getId()))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> transactionService.getTransactionByIdAndAccountId(depositTransaction.getId(), testAccount.getId(), testUser.getId()));

        assertEquals("Account not found or not associated with your user.", exception.getMessage());
        verify(transactionRepository, never()).findByIdAndAccountId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("getTransactionByIdAndAccountId: Should throw ResourceNotFoundException if transaction not found for account")
    void getTransactionByIdAndAccountId_TransactionNotFound() {
        when(accountRepository.findByIdAndUserId(testAccount.getId(), testUser.getId()))
                .thenReturn(Optional.of(testAccount));
        when(transactionRepository.findByIdAndAccountId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> transactionService.getTransactionByIdAndAccountId(99L, testAccount.getId(), testUser.getId()));

        assertEquals("Transaction not found for this account.", exception.getMessage());
        verify(accountRepository, times(1)).findByIdAndUserId(testAccount.getId(), testUser.getId());
        verify(transactionRepository, times(1)).findByIdAndAccountId(99L, testAccount.getId());
    }

    @Test
    @DisplayName("getTransactionByIdAndAccountId: Should throw ResourceNotFoundException if transaction exists but belongs to different account")
    void getTransactionByIdAndAccountId_TransactionWrongAccount() {
        // Scenario where transaction ID is found, but not for the specific accountId provided.
        // This is handled by findByIdAndAccountId returning empty.
        when(accountRepository.findByIdAndUserId(testAccount.getId(), testUser.getId()))
                .thenReturn(Optional.of(testAccount));
        when(transactionRepository.findByIdAndAccountId(depositTransaction.getId(), testAccount.getId()))
                .thenReturn(Optional.empty()); // Simulate not found for this specific account

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> transactionService.getTransactionByIdAndAccountId(depositTransaction.getId(), testAccount.getId(), testUser.getId()));

        assertEquals("Transaction not found for this account.", exception.getMessage());
        verify(accountRepository, times(1)).findByIdAndUserId(testAccount.getId(), testUser.getId());
        verify(transactionRepository, times(1)).findByIdAndAccountId(depositTransaction.getId(), testAccount.getId());
    }
}
