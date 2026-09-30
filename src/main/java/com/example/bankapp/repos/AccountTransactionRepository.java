package com.example.bankapp.repos;

import java.util.Collection;
import java.util.List;

import com.example.bankapp.models.AccountTransaction;

public interface AccountTransactionRepository {

    List<AccountTransaction> findByAccountId(String accountId);

    AccountTransaction save(AccountTransaction transaction);

    long deleteByAccountIdIn(Collection<String> accountIds);
}