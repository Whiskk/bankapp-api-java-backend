package com.example.bankapp.repos;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.UUID;

import com.example.bankapp.models.AccountTransaction;

public class InMemoryAccountTransactionRepository implements AccountTransactionRepository {

    private final CopyOnWriteArrayList<AccountTransaction> transactions = new CopyOnWriteArrayList<>();

    @Override
    public List<AccountTransaction> findByAccountId(String accountId) {
        return transactions.stream()
                .filter(transaction -> transaction.accountId().equals(accountId))
                .toList();
    }

    @Override
    public AccountTransaction save(AccountTransaction transaction) {
        AccountTransaction savedTransaction = new AccountTransaction(
                UUID.randomUUID().toString(),
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