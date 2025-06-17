package com.eaglebank.api.controller;

import com.eaglebank.api.dto.CreateTransactionRequest;
import com.eaglebank.api.dto.ListTransactionsResponse;
import com.eaglebank.api.dto.TransactionResponse;
import com.eaglebank.api.model.Transaction;
import com.eaglebank.api.model.User;
import com.eaglebank.api.repository.UserRepository;
import com.eaglebank.api.service.TransactionService;
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
@RequestMapping("/v1/accounts/{accountNumber}/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @PathVariable String accountNumber,
            @Valid @RequestBody CreateTransactionRequest request,
            Principal principal) {

        Optional<User> authenticatedUser = userRepository.findByUsername(principal.getName());
        String authenticatedUserId = authenticatedUser.map(User::getId)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in DB. This should not happen."));

        Transaction createdTransaction = transactionService.performTransaction(accountNumber, authenticatedUserId, request);
        TransactionResponse response = new TransactionResponse(
                createdTransaction.getId(),
                createdTransaction.getAmount(),
                createdTransaction.getCurrency(),
                createdTransaction.getType(),
                createdTransaction.getReference(),
                createdTransaction.getUserId(),
                createdTransaction.getCreatedTimestamp()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ListTransactionsResponse> listAccountTransactions(
            @PathVariable String accountNumber,
            Principal principal) {

        Optional<User> authenticatedUser = userRepository.findByUsername(principal.getName());
        String authenticatedUserId = authenticatedUser.map(User::getId)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in DB. This should not happen."));

        List<Transaction> transactions = transactionService.getTransactionsByAccountNumber(accountNumber, authenticatedUserId);
        List<TransactionResponse> transactionResponses = transactions.stream()
                .map(transaction -> new TransactionResponse(
                        transaction.getId(),
                        transaction.getAmount(),
                        transaction.getCurrency(),
                        transaction.getType(),
                        transaction.getReference(),
                        transaction.getUserId(),
                        transaction.getCreatedTimestamp()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ListTransactionsResponse(transactionResponses));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> fetchAccountTransactionByID(
            @PathVariable String accountNumber,
            @PathVariable String transactionId,
            Principal principal) {

        Optional<User> authenticatedUser = userRepository.findByUsername(principal.getName());
        String authenticatedUserId = authenticatedUser.map(User::getId)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in DB. This should not happen."));

        Transaction transaction = transactionService.getTransactionByIdAndAccountNumber(transactionId, accountNumber, authenticatedUserId);
        TransactionResponse response = new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getType(),
                transaction.getReference(),
                transaction.getUserId(),
                transaction.getCreatedTimestamp()
        );
        return ResponseEntity.ok(response);
    }
}
