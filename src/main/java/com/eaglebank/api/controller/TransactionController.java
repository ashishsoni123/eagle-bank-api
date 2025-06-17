package com.eaglebank.api.controller;

import com.eaglebank.api.dto.TransactionRequest;
import com.eaglebank.api.model.Transaction;
import com.eaglebank.api.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/v1/accounts/{accountId}/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@PathVariable Long accountId,
                                                         @Valid @RequestBody TransactionRequest transactionRequest,
                                                         Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        Transaction transaction = transactionService.performTransaction(accountId, userId, transactionRequest);
        return new ResponseEntity<>(transaction, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getTransactionsForAccount(@PathVariable Long accountId, Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        List<Transaction> transactions = transactionService.getTransactionsByAccountId(accountId, userId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long accountId,
                                                          @PathVariable Long transactionId,
                                                          Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        Transaction transaction = transactionService.getTransactionByIdAndAccountId(transactionId, accountId, userId);
        return ResponseEntity.ok(transaction);
    }
}