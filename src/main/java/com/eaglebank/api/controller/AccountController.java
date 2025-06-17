package com.eaglebank.api.controller;

import com.eaglebank.api.dto.BankAccountResponse;
import com.eaglebank.api.dto.CreateBankAccountRequest;
import com.eaglebank.api.dto.ListBankAccountsResponse;
import com.eaglebank.api.dto.UpdateBankAccountRequest;
import com.eaglebank.api.model.Account;
import com.eaglebank.api.model.User;
import com.eaglebank.api.repository.UserRepository;
import com.eaglebank.api.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<BankAccountResponse> createAccount(
            @Valid @RequestBody CreateBankAccountRequest request,
            Principal principal) {

        Optional<User> authenticatedUser = userRepository.findByUsername(principal.getName());
        String authenticatedUserId = authenticatedUser.map(User::getId)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in DB. This should not happen."));

        Account createdAccount = accountService.createAccount(authenticatedUserId, request);
        BankAccountResponse response = new BankAccountResponse(
                createdAccount.getAccountNumber(),
                createdAccount.getSortCode(),
                createdAccount.getName(),
                createdAccount.getAccountType(),
                createdAccount.getBalance(),
                createdAccount.getCurrency(),
                createdAccount.getCreatedTimestamp(),
                createdAccount.getUpdatedTimestamp()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ListBankAccountsResponse> listAccounts(Principal principal) {
        Optional<User> authenticatedUser = userRepository.findByUsername(principal.getName());
        String authenticatedUserId = authenticatedUser.map(User::getId)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in DB. This should not happen."));

        List<Account> accounts = accountService.getAccountsByUserId(authenticatedUserId);
        List<BankAccountResponse> accountResponses = accounts.stream()
                .map(account -> new BankAccountResponse(
                        account.getAccountNumber(),
                        account.getSortCode(),
                        account.getName(),
                        account.getAccountType(),
                        account.getBalance(),
                        account.getCurrency(),
                        account.getCreatedTimestamp(),
                        account.getUpdatedTimestamp()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ListBankAccountsResponse(accountResponses));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<BankAccountResponse> fetchAccountByAccountNumber(
            @PathVariable String accountNumber,
            Principal principal) {

        Optional<User> authenticatedUser = userRepository.findByUsername(principal.getName());
        String authenticatedUserId = authenticatedUser.map(User::getId)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in DB. This should not happen."));

        Account account = accountService.getAccountByAccountNumberAndUserId(accountNumber, authenticatedUserId);
        BankAccountResponse response = new BankAccountResponse(
                account.getAccountNumber(),
                account.getSortCode(),
                account.getName(),
                account.getAccountType(),
                account.getBalance(),
                account.getCurrency(),
                account.getCreatedTimestamp(),
                account.getUpdatedTimestamp()
        );
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{accountNumber}")
    public ResponseEntity<BankAccountResponse> updateAccountByAccountNumber(
            @PathVariable String accountNumber,
            Principal principal,
            @Valid @RequestBody UpdateBankAccountRequest request) {

        Optional<User> authenticatedUser = userRepository.findByUsername(principal.getName());
        String authenticatedUserId = authenticatedUser.map(User::getId)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in DB. This should not happen."));

        Account updatedAccount = accountService.updateAccount(accountNumber, authenticatedUserId, request);
        BankAccountResponse response = new BankAccountResponse(
                updatedAccount.getAccountNumber(),
                updatedAccount.getSortCode(),
                updatedAccount.getName(),
                updatedAccount.getAccountType(),
                updatedAccount.getBalance(),
                updatedAccount.getCurrency(),
                updatedAccount.getCreatedTimestamp(),
                updatedAccount.getUpdatedTimestamp()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<Void> deleteAccountByAccountNumber(
            @PathVariable String accountNumber,
            Principal principal) {

        Optional<User> authenticatedUser = userRepository.findByUsername(principal.getName());
        String authenticatedUserId = authenticatedUser.map(User::getId)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in DB. This should not happen."));

        accountService.deleteAccount(accountNumber, authenticatedUserId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
