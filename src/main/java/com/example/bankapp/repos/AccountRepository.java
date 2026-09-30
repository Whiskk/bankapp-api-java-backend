package com.example.bankapp.repos;

import java.util.List;
import java.util.Optional;

import com.example.bankapp.models.Account;

public interface AccountRepository {

    Optional<Account> findById(String id);

    List<Account> findByUserId(String userId);

    Account save(Account account);

    boolean deleteByUserId(String userId);
}