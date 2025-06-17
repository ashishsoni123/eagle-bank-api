package com.eaglebank.api.repository;

import com.eaglebank.api.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountId(Long accountId);
    Optional<Transaction> findByIdAndAccountId(Long transactionId, Long accountId);
}