package com.eaglebank.api.service;

import com.eaglebank.api.dto.AccountDto;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountService accountService;

    private User testUser;
    private Account testAccount;
    private AccountDto testAccountDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setAccountNumber("1012345678");
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setUser(testUser);

        testAccountDto = new AccountDto();
        testAccountDto.setInitialBalance(new BigDecimal("1000.00"));
    }

    @Test
    void createAccount_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(accountRepository.existsByAccountNumber(any())).thenReturn(false);

        Account createdAccount = accountService.createAccount(1L, testAccountDto);

        assertNotNull(createdAccount);
        assertEquals(testUser, createdAccount.getUser());
        assertEquals(testAccountDto.getInitialBalance(), createdAccount.getBalance());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.createAccount(1L, testAccountDto));

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void getAccountsByUserId_Success() {
        List<Account> accounts = Collections.singletonList(testAccount);
        when(accountRepository.findByUserId(1L)).thenReturn(accounts);

        List<Account> result = accountService.getAccountsByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAccount, result.get(0));
    }

    @Test
    void getAccountByIdAndUserId_Success() {
        when(accountRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(testAccount));

        Account result = accountService.getAccountByIdAndUserId(1L, 1L);

        assertNotNull(result);
        assertEquals(testAccount.getId(), result.getId());
        assertEquals(testAccount.getAccountNumber(), result.getAccountNumber());
    }

    @Test
    void getAccountByIdAndUserId_NotFound() {
        when(accountRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.getAccountByIdAndUserId(1L, 1L));
    }
}
