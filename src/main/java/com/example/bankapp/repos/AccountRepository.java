package com.example.bankapp.repos;

import java.util.Optional;

import com.example.bankapp.models.Account;

public interface AccountRepository {

    Optional<Account> findById(Long id);

    Account save(Account account);
}