package com.eaglebank.api.controller;

import com.eaglebank.api.dto.AccountDto;
import com.eaglebank.api.model.Account;
import com.eaglebank.api.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<Account> createAccount(@Valid @RequestBody AccountDto accountDto, Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        Account createdAccount = accountService.createAccount(userId, accountDto);
        return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Account>> getAllAccountsForUser(Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        List<Account> accounts = accountService.getAccountsByUserId(userId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<Account> getAccountById(@PathVariable Long accountId, Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        Account account = accountService.getAccountByIdAndUserId(accountId, userId);
        return ResponseEntity.ok(account);
    }
    // Implement PATCH for update, DELETE for delete following scenarios.
}