package com.eaglebank.api.controller;

import com.eaglebank.api.dto.CreateTransactionRequest;
import com.eaglebank.api.dto.TransactionsResponse;
import com.eaglebank.api.dto.TransactionResponse;
import com.eaglebank.api.model.Transaction;
import com.eaglebank.api.repository.UserRepository;
import com.eaglebank.api.service.TransactionService;
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
@RequestMapping("/v1/accounts/{accountNumber}/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createTransaction(
            @PathVariable String accountNumber,
            @Valid @RequestBody CreateTransactionRequest createTransactionRequest,
            HttpServletRequest request) {

        String authenticatedUserId = (String) request.getAttribute(AUTHENTICATED_USER_ID);

        Transaction createdTransaction = transactionService.performTransaction(accountNumber, authenticatedUserId, createTransactionRequest);
        TransactionResponse response = new TransactionResponse();
        BeanUtils.copyProperties(createdTransaction, response);
        return response;
    }

    @GetMapping
    public TransactionsResponse listAccountTransactions(
            @PathVariable String accountNumber,
            HttpServletRequest request) {


        String authenticatedUserId = (String) request.getAttribute(AUTHENTICATED_USER_ID);
        List<Transaction> transactions = transactionService.getTransactionsByAccountNumber(accountNumber, authenticatedUserId);
        List<TransactionResponse> transactionResponses = transactions.stream()
                .map(transaction -> {
                    TransactionResponse response = new TransactionResponse();
                    BeanUtils.copyProperties(transaction, response);
                    return response;
                })
                .collect(Collectors.toList());
        return new TransactionsResponse(transactionResponses);
    }

    @GetMapping("/{transactionId}")
    public TransactionResponse fetchAccountTransactionByID(
            @PathVariable String accountNumber,
            @PathVariable String transactionId,
            HttpServletRequest request) {


        String authenticatedUserId = (String) request.getAttribute(AUTHENTICATED_USER_ID);
        Transaction transaction = transactionService.getTransactionByIdAndAccountNumber(transactionId, accountNumber, authenticatedUserId);
        TransactionResponse response = new TransactionResponse();
        BeanUtils.copyProperties(transaction, response);
        return response;
    }
}
