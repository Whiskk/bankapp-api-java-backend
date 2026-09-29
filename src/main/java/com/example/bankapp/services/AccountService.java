package com.example.bankapp.services;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.bankapp.models.Account;
import com.example.bankapp.models.AccountRequest;
import com.example.bankapp.models.AccountTransaction;
import com.example.bankapp.models.MoneyRequest;
import com.example.bankapp.models.TransactionType;
import com.example.bankapp.repos.AccountRepository;
import com.example.bankapp.repos.AccountTransactionRepository;
import com.example.bankapp.repos.CustomerRepository;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountTransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;

    public AccountService(
            AccountRepository accountRepository,
            AccountTransactionRepository transactionRepository,
            CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.customerRepository = customerRepository;
    }

    public Account createAccount(AccountRequest request) {
        if (customerRepository.findById(request.userId()).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Customer %d was not found".formatted(request.userId()));
        }
        return accountRepository.save(new Account(null, request.userId(), request.accountType(), BigDecimal.ZERO));
    }

    public Account getAccountById(Long id) {
        return findAccount(id);
    }

    public Account deposit(Long id, MoneyRequest request) {
        return applyTransaction(id, request.amount(), TransactionType.DEPOSIT);
    }

    public Account withdraw(Long id, MoneyRequest request) {
        Account account = findAccount(id);
        if (account.balance().compareTo(request.amount()) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        }
        return applyTransaction(id, request.amount(), TransactionType.WITHDRAWAL);
    }

    public List<AccountTransaction> getTransactions(Long id) {
        findAccount(id);
        return transactionRepository.findByAccountId(id);
    }

    private Account applyTransaction(Long id, BigDecimal amount, TransactionType type) {
        Account account = findAccount(id);
        BigDecimal balanceChange = type == TransactionType.DEPOSIT ? amount : amount.negate();
        BigDecimal newBalance = account.balance().add(balanceChange);
        Account updatedAccount = accountRepository.save(new Account(
                account.id(), account.userId(), account.accountType(), newBalance));
        transactionRepository.save(new AccountTransaction(
                null, id, type, amount, newBalance, Instant.now()));
        return updatedAccount;
    }

    private Account findAccount(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Account %d was not found".formatted(id)));
    }
}