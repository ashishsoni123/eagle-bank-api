package com.eaglebank.api.repository;

import com.eaglebank.api.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    List<Account> findByUserId(String userId);
    Optional<Account> findByAccountNumberAndUserId(String accountNumber, String userId);
    Optional<Account> findByAccountNumber(String accountNumber);
}
