package com.eaglebank.api.repository;

import com.eaglebank.api.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserId(Long userId);
    Optional<Account> findByIdAndUserId(Long accountId, Long userId);
    boolean existsByAccountNumber(String accountNumber);
}
