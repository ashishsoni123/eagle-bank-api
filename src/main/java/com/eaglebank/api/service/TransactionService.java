package com.eaglebank.api.service;

import com.eaglebank.api.dto.TransactionRequest;
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

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public Transaction performTransaction(Long accountId, Long userId, TransactionRequest transactionRequest) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found or not associated with your user."));

        BigDecimal amount = transactionRequest.getAmount();
        String type = transactionRequest.getType();

        if (type.equalsIgnoreCase("DEPOSIT")) {
            account.setBalance(account.getBalance().add(amount));
        } else if (type.equalsIgnoreCase("WITHDRAWAL")) {
            if (account.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException("Insufficient funds for withdrawal.");
            }
            account.setBalance(account.getBalance().subtract(amount));
        } else {
            throw new BadRequestException("Invalid transaction type: " + type);
        }

        accountRepository.save(account); // Save updated account balance

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setAccount(account);
        return transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactionsByAccountId(Long accountId, Long userId) {
        accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found or not associated with your user."));
        return transactionRepository.findByAccountId(accountId);
    }

    public Transaction getTransactionByIdAndAccountId(Long transactionId, Long accountId, Long userId) {
        accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found or not associated with your user."));

        return transactionRepository.findByIdAndAccountId(transactionId, accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found for this account."));
    }
}