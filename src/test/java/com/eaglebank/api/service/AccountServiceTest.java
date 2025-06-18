package com.eaglebank.api.service;

import com.eaglebank.api.dto.CreateAccountRequest;
import com.eaglebank.api.dto.UpdateAccountRequest;
import com.eaglebank.api.exceptiom.ResourceNotFoundException;
import com.eaglebank.api.model.Account;
import com.eaglebank.api.model.User;
import com.eaglebank.api.repository.AccountRepository;
import com.eaglebank.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountService accountService;

    private User testUser;
    private Account testAccount;
    private String userId;
    private String accountNumber;

    @BeforeEach
    void setUp() {
        userId = "test-user-id";
        accountNumber = "1234567890";

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");

        testAccount = new Account();
        testAccount.setAccountNumber(accountNumber);
        testAccount.setName("Test Account");
        testAccount.setAccountType("SAVINGS");
        testAccount.setBalance(BigDecimal.ZERO);
        testAccount.setCurrency("GBP");
        testAccount.setUser(testUser);
    }

    @Test
    void createAccount_Success() {
        // Arrange
        CreateAccountRequest request = new CreateAccountRequest();
        request.setName("Test Account");
        request.setAccountType("SAVINGS");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // Act
        Account result = accountService.createAccount(userId, request);

        // Assert
        assertNotNull(result);
        assertEquals("Test Account", result.getName());
        assertEquals("SAVINGS", result.getAccountType());
        assertEquals(BigDecimal.ZERO, result.getBalance());
        assertEquals("GBP", result.getCurrency());
        verify(userRepository).findById(userId);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_UserNotFound() {
        // Arrange
        CreateAccountRequest request = new CreateAccountRequest();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> accountService.createAccount(userId, request));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void getAccountsByUserId_Success() {
        // Arrange
        List<Account> accounts = Collections.singletonList(testAccount);
        when(accountRepository.findByUserId(userId)).thenReturn(accounts);

        // Act
        List<Account> result = accountService.getAccountsByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAccount.getAccountNumber(), result.get(0).getAccountNumber());
        verify(accountRepository).findByUserId(userId);
    }

    @Test
    void getAccountByAccountNumberAndUserId_Success() {
        // Arrange
        when(accountRepository.findByAccountNumberAndUserId(accountNumber, userId))
                .thenReturn(Optional.of(testAccount));

        // Act
        Account result = accountService.getAccountByAccountNumberAndUserId(accountNumber, userId);

        // Assert
        assertNotNull(result);
        assertEquals(accountNumber, result.getAccountNumber());
        assertEquals(testUser.getId(), result.getUser().getId());
        verify(accountRepository).findByAccountNumberAndUserId(accountNumber, userId);
    }

    @Test
    void getAccountByAccountNumberAndUserId_NotFound() {
        // Arrange
        when(accountRepository.findByAccountNumberAndUserId(accountNumber, userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> accountService.getAccountByAccountNumberAndUserId(accountNumber, userId));
    }

    @Test
    void updateAccount_Success() {
        // Arrange
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setName("Updated Account Name");
        request.setAccountType("CURRENT");

        when(accountRepository.findByAccountNumberAndUserId(accountNumber, userId))
                .thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // Act
        Account result = accountService.updateAccount(accountNumber, userId, request);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Account Name", result.getName());
        assertEquals("CURRENT", result.getAccountType());
        verify(accountRepository).findByAccountNumberAndUserId(accountNumber, userId);
        verify(accountRepository).save(testAccount);
    }

    @Test
    void updateAccount_NotFound() {
        // Arrange
        UpdateAccountRequest request = new UpdateAccountRequest();
        when(accountRepository.findByAccountNumberAndUserId(accountNumber, userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> accountService.updateAccount(accountNumber, userId, request));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void deleteAccount_Success() {
        // Arrange
        when(accountRepository.findByAccountNumberAndUserId(accountNumber, userId))
                .thenReturn(Optional.of(testAccount));

        // Act
        accountService.deleteAccount(accountNumber, userId);

        // Assert
        verify(accountRepository).findByAccountNumberAndUserId(accountNumber, userId);
        verify(accountRepository).delete(testAccount);
    }

    @Test
    void deleteAccount_NotFound() {
        // Arrange
        when(accountRepository.findByAccountNumberAndUserId(accountNumber, userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> accountService.deleteAccount(accountNumber, userId));
        verify(accountRepository, never()).delete(any());
    }
}
