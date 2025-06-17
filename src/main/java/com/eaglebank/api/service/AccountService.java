package com.eaglebank.api.service;

import com.eaglebank.api.dto.AccountDto;
import com.eaglebank.api.exceptiom.ResourceNotFoundException;
import com.eaglebank.api.model.Account;
import com.eaglebank.api.model.User;
import com.eaglebank.api.repository.AccountRepository;
import com.eaglebank.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public Account createAccount(Long userId, AccountDto accountDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Account account = new Account();
        account.setAccountNumber(generateUniqueAccountNumber()); // Implement this method
        account.setBalance(accountDto.getInitialBalance() != null ? accountDto.getInitialBalance() : BigDecimal.ZERO);
        account.setUser(user);
        return accountRepository.save(account);
    }

    public List<Account> getAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    public Account getAccountByIdAndUserId(Long accountId, Long userId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found or not associated with your user."));
    }

    private String generateUniqueAccountNumber() {
        Random random = new Random();
        StringBuilder accountNumber = new StringBuilder("10"); // Starting with 10 to indicate it's a bank account

        // Generate 8 more random digits
        for (int i = 0; i < 8; i++) {
            accountNumber.append(random.nextInt(10));
        }

        // Verify uniqueness by checking if account number already exists
        while (accountRepository.existsByAccountNumber(accountNumber.toString())) {
            accountNumber = new StringBuilder("10");
            for (int i = 0; i < 8; i++) {
                accountNumber.append(random.nextInt(10));
            }
        }

        return accountNumber.toString();
    }

    // Implement update and delete methods.
}