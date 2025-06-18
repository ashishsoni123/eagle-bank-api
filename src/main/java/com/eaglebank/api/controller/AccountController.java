package com.eaglebank.api.controller;

import com.eaglebank.api.dto.AccountResponse;
import com.eaglebank.api.dto.CreateAccountRequest;
import com.eaglebank.api.dto.AccountsResponse;
import com.eaglebank.api.dto.UpdateAccountRequest;
import com.eaglebank.api.model.Account;
import com.eaglebank.api.repository.UserRepository;
import com.eaglebank.api.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.eaglebank.api.security.JwtRequestFilter.AUTHENTICATED_USER_ID;

@RestController
@RequestMapping("/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final UserRepository userRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            HttpServletRequest httpServletRequest) {

        String authenticatedUserId = (String) httpServletRequest.getAttribute(AUTHENTICATED_USER_ID);
        Account createdAccount = accountService.createAccount(authenticatedUserId, request);
        AccountResponse response = new AccountResponse();
        BeanUtils.copyProperties(createdAccount, response);
        return response;
    }

    @GetMapping
    public AccountsResponse listAccounts(HttpServletRequest request) {

        String authenticatedUserId = (String) request.getAttribute(AUTHENTICATED_USER_ID);
        List<Account> accounts = accountService.getAccountsByUserId(authenticatedUserId);
        List<AccountResponse> accountResponses = accounts.stream()
                .map(account -> {
                    AccountResponse response = new AccountResponse();
                    BeanUtils.copyProperties(account, response);
                    return response;
                })
                .collect(Collectors.toList());
        return new AccountsResponse(accountResponses);
    }

    @GetMapping("/{accountNumber}")
    public AccountResponse fetchAccountByAccountNumber(
            @PathVariable String accountNumber,
            HttpServletRequest request) {


        String authenticatedUserId = (String) request.getAttribute(AUTHENTICATED_USER_ID);
        Account account = accountService.getAccountByAccountNumberAndUserId(accountNumber, authenticatedUserId);
        AccountResponse response = new AccountResponse();
        BeanUtils.copyProperties(account, response);
        return response;
    }

    @PatchMapping("/{accountNumber}")
    public AccountResponse updateAccountByAccountNumber(
            @PathVariable String accountNumber,
            HttpServletRequest request,
            @Valid @RequestBody UpdateAccountRequest updateAccountRequest) {

        String authenticatedUserId = (String) request.getAttribute(AUTHENTICATED_USER_ID);

        Account updatedAccount = accountService.updateAccount(accountNumber, authenticatedUserId, updateAccountRequest);
        AccountResponse response = new AccountResponse();
        BeanUtils.copyProperties(updatedAccount, response);
        return response;
    }

    @DeleteMapping("/{accountNumber}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccountByAccountNumber(
            @PathVariable String accountNumber,
            HttpServletRequest request) {

        String authenticatedUserId = (String) request.getAttribute(AUTHENTICATED_USER_ID);

        accountService.deleteAccount(accountNumber, authenticatedUserId);
    }
}
