package com.example.bankapp.repos;

import java.util.List;

import com.example.bankapp.models.AccountTransaction;

public interface AccountTransactionRepository {

    List<AccountTransaction> findByAccountId(Long accountId);

    AccountTransaction save(AccountTransaction transaction);
}