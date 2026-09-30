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
import com.example.bankapp.models.TransferRequest;
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

    public Account getAccountById(String id) {
        return findAccount(id);
    }

    public List<Account> getAccountsForCustomer(String userId) {
        return accountRepository.findByUserId(userId);
    }

    public Account deposit(String id, MoneyRequest request) {
        return applyTransaction(id, request.amount(), TransactionType.DEPOSIT);
    }

    public Account withdraw(String id, MoneyRequest request) {
        Account account = findAccount(id);
        if (account.balance().compareTo(request.amount()) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        }
        return applyTransaction(id, request.amount(), TransactionType.WITHDRAWAL);
    }

    public Account transfer(TransferRequest request) {
        Account sourceAccount = findAccount(request.fromAccountId());
        Account targetAccount = findAccount(request.toAccountId());

        if (sourceAccount.id().equals(targetAccount.id())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Source and target accounts must be different");
        }
        if (!sourceAccount.userId().equals(targetAccount.userId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Accounts must belong to the same customer");
        }
        if (sourceAccount.balance().compareTo(request.amount()) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        }

        BigDecimal sourceBalance = sourceAccount.balance().subtract(request.amount());
        BigDecimal targetBalance = targetAccount.balance().add(request.amount());

        Account updatedSource = accountRepository.save(new Account(
                sourceAccount.id(), sourceAccount.userId(), sourceAccount.accountType(), sourceBalance));
        accountRepository.save(new Account(
                targetAccount.id(), targetAccount.userId(), targetAccount.accountType(), targetBalance));

        Instant timestamp = Instant.now();
        transactionRepository.save(new AccountTransaction(
                null, sourceAccount.id(), TransactionType.TRANSFER, request.amount(),
                sourceBalance, targetAccount.id(), timestamp));
        transactionRepository.save(new AccountTransaction(
                null, targetAccount.id(), TransactionType.TRANSFER, request.amount(),
                targetBalance, sourceAccount.id(), timestamp));

        return updatedSource;
    }

    public List<AccountTransaction> getTransactions(String id) {
        findAccount(id);
        return transactionRepository.findByAccountId(id);
    }

    private Account applyTransaction(String id, BigDecimal amount, TransactionType type) {
        Account account = findAccount(id);
        BigDecimal balanceChange = type == TransactionType.DEPOSIT ? amount : amount.negate();
        BigDecimal newBalance = account.balance().add(balanceChange);
        Account updatedAccount = accountRepository.save(new Account(
                account.id(), account.userId(), account.accountType(), newBalance));
        transactionRepository.save(new AccountTransaction(
            null, id, type, amount, newBalance, null, Instant.now()));
        return updatedAccount;
    }

    private Account findAccount(String id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Account %s was not found".formatted(id)));
    }
}