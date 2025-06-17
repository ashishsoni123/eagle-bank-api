package com.eaglebank.api.service;

import com.eaglebank.api.dto.CreateTransactionRequest;
import com.eaglebank.api.exceptiom.BadRequestException;
import com.eaglebank.api.exceptiom.InsufficientFundsException;
import com.eaglebank.api.exceptiom.ResourceNotFoundException;
import com.eaglebank.api.model.Account;
import com.eaglebank.api.model.Transaction;
import com.eaglebank.api.repository.AccountRepository;
import com.eaglebank.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public Transaction performTransaction(String accountNumber, String authenticatedUserId, CreateTransactionRequest request) {
        Account account = accountRepository.findByAccountNumberAndUserId(accountNumber, authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account was not found or not associated with your user."));

        BigDecimal amount = request.getAmount();
        String type = request.getType();

        if (type.equalsIgnoreCase("deposit")) {
            account.setBalance(account.getBalance().add(amount));
        } else if (type.equalsIgnoreCase("withdrawal")) {
            if (account.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException("Insufficient funds to process transaction.");
            }
            account.setBalance(account.getBalance().subtract(amount));
        } else {
            throw new BadRequestException("Invalid transaction type: " + type);
        }

        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setId("tan-" + UUID.randomUUID().toString().substring(0, 8));
        transaction.setAmount(amount);
        transaction.setCurrency(request.getCurrency());
        transaction.setType(type);
        transaction.setReference(request.getReference());
        transaction.setUserId(authenticatedUserId);
        transaction.setCreatedTimestamp(LocalDateTime.now());
        transaction.setAccount(account);

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactionsByAccountNumber(String accountNumber, String authenticatedUserId) {
        accountRepository.findByAccountNumberAndUserId(accountNumber, authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account was not found or not associated with your user."));

        return transactionRepository.findByAccountAccountNumber(accountNumber);
    }

    public Transaction getTransactionByIdAndAccountNumber(String transactionId, String accountNumber, String authenticatedUserId) {
        accountRepository.findByAccountNumberAndUserId(accountNumber, authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account was not found or not associated with your user."));

        return transactionRepository.findByIdAndAccountAccountNumber(transactionId, accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction was not found."));
    }
}
