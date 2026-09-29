package com.example.bankapp.repos;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.example.bankapp.models.AccountTransaction;

@Repository
public class InMemoryAccountTransactionRepository implements AccountTransactionRepository {

    private final AtomicLong nextId = new AtomicLong(1);
    private final CopyOnWriteArrayList<AccountTransaction> transactions = new CopyOnWriteArrayList<>();

    @Override
    public List<AccountTransaction> findByAccountId(Long accountId) {
        return transactions.stream()
                .filter(transaction -> transaction.accountId().equals(accountId))
                .toList();
    }

    @Override
    public AccountTransaction save(AccountTransaction transaction) {
        AccountTransaction savedTransaction = new AccountTransaction(
                nextId.getAndIncrement(),
                transaction.accountId(),
                transaction.type(),
                transaction.amount(),
                transaction.balanceAfter(),
                transaction.relatedAccountId(),
                transaction.createdAt());
        transactions.add(savedTransaction);
        return savedTransaction;
    }
}