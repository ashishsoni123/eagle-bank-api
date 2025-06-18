package com.eaglebank.api.service;

import com.eaglebank.api.dto.CreateBankAccountRequest;
import com.eaglebank.api.dto.UpdateBankAccountRequest;
import com.eaglebank.api.exceptiom.ResourceNotFoundException;
import com.eaglebank.api.model.Account;
import com.eaglebank.api.model.User;
import com.eaglebank.api.repository.AccountRepository;
import com.eaglebank.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AccountService {

    public static final String GBP = "GBP";
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private static final Random RANDOM = new Random();

    @Transactional
    public Account createAccount(String userId, CreateBankAccountRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Account account = new Account();
        account.setAccountNumber(generateUniqueAccountNumber());
        account.setSortCode(generateSortCode());
        account.setName(request.getName());
        account.setAccountType(request.getAccountType());
        account.setBalance(BigDecimal.ZERO);
        account.setCurrency(GBP);
        account.setUser(user);

        return accountRepository.save(account);
    }

    public List<Account> getAccountsByUserId(String userId) {
        return accountRepository.findByUserId(userId);
    }

    public Account getAccountByAccountNumberAndUserId(String accountNumber, String authenticatedUserId) {
        return accountRepository.findByAccountNumberAndUserId(accountNumber, authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account was not found or not associated with your user."));
    }

    @Transactional
    public Account updateAccount(String accountNumber, String authenticatedUserId, UpdateBankAccountRequest request) {
        Account account = accountRepository.findByAccountNumberAndUserId(accountNumber, authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account was not found or not associated with your user."));

        if (request.getName() != null && !request.getName().isBlank()) {
            account.setName(request.getName());
        }
        if (request.getAccountType() != null && !request.getAccountType().isBlank()) {
            account.setAccountType(request.getAccountType());
        }

        return accountRepository.save(account);
    }

    @Transactional
    public void deleteAccount(String accountNumber, String authenticatedUserId) {
        Account account = accountRepository.findByAccountNumberAndUserId(accountNumber, authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account was not found or not associated with your user."));

        accountRepository.delete(account);
    }

    private String generateUniqueAccountNumber() {
        StringBuilder sb = new StringBuilder("01");
        for (int i = 0; i < 6; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    private String generateSortCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            if (i > 0) sb.append("-");
            int part = RANDOM.nextInt(90) + 10; // ensures two digits, not starting with 0
            sb.append(part);
        }
        return sb.toString();
    }
}
