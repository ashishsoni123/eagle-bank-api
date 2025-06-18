package com.eaglebank.api.service;

import com.eaglebank.api.dto.CreateAccountRequest;
import com.eaglebank.api.dto.UpdateAccountRequest;
import com.eaglebank.api.exceptiom.ResourceNotFoundException;
import com.eaglebank.api.model.Account;
import com.eaglebank.api.model.User;
import com.eaglebank.api.repository.AccountRepository;
import com.eaglebank.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.eaglebank.api.util.AccountUtils.generateSortCode;
import static com.eaglebank.api.util.AccountUtils.generateUniqueAccountNumber;

@Service
@RequiredArgsConstructor
public class AccountService {

    public static final String GBP = "GBP";
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional
    public Account createAccount(String userId, CreateAccountRequest request) {
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
    public Account updateAccount(String accountNumber, String authenticatedUserId, UpdateAccountRequest request) {
        Account account = accountRepository.findByAccountNumberAndUserId(accountNumber, authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account was not found or not associated with your user."));

        if (StringUtils.isNotBlank(request.getName()))  {
            account.setName(request.getName());
        }
        if (StringUtils.isNotBlank(request.getAccountType())) {
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
}
