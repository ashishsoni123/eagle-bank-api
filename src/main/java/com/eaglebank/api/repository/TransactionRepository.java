package com.eaglebank.api.repository;

import com.eaglebank.api.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findByAccountAccountNumber(String accountNumber);
    Optional<Transaction> findByIdAndAccountAccountNumber(String transactionId, String accountNumber);
}
