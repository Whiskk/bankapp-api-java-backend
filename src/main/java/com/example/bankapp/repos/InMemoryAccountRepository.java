package com.example.bankapp.repos;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.UUID;

import com.example.bankapp.models.Account;

public class InMemoryAccountRepository implements AccountRepository {

    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public Optional<Account> findById(String id) {
        return Optional.ofNullable(accounts.get(id));
    }

    @Override
    public Account save(Account account) {
        String id = account.id() == null ? UUID.randomUUID().toString() : account.id();
        Account savedAccount = new Account(id, account.userId(), account.accountType(), account.balance());
        accounts.put(id, savedAccount);
        return savedAccount;
    }
}