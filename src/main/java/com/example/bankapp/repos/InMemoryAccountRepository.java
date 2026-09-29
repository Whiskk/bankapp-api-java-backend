package com.example.bankapp.repos;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.example.bankapp.models.Account;

@Repository
public class InMemoryAccountRepository implements AccountRepository {

    private final AtomicLong nextId = new AtomicLong(1);
    private final ConcurrentMap<Long, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public Optional<Account> findById(Long id) {
        return Optional.ofNullable(accounts.get(id));
    }

    @Override
    public Account save(Account account) {
        Long id = account.id() == null ? nextId.getAndIncrement() : account.id();
        Account savedAccount = new Account(id, account.userId(), account.accountType(), account.balance());
        accounts.put(id, savedAccount);
        return savedAccount;
    }
}