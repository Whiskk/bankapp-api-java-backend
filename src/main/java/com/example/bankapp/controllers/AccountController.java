package com.example.bankapp.controllers;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.bankapp.models.Account;
import com.example.bankapp.models.AccountRequest;
import com.example.bankapp.models.AccountTransaction;
import com.example.bankapp.models.MoneyRequest;
import com.example.bankapp.models.TransferRequest;
import com.example.bankapp.services.AccountService;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@Valid @RequestBody AccountRequest request) {
        Account account = accountService.createAccount(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(account.id())
                .toUri();
        return ResponseEntity.created(location).body(account);
    }

    @GetMapping("/{id}")
    public Account getAccountDetails(@PathVariable String id) {
        return accountService.getAccountById(id);
    }

    @PostMapping("/{id}/deposit")
    public Account depositMoney(@PathVariable String id, @Valid @RequestBody MoneyRequest request) {
        return accountService.deposit(id, request);
    }

    @PostMapping("/{id}/withdraw")
    public Account withdrawMoney(@PathVariable String id, @Valid @RequestBody MoneyRequest request) {
        return accountService.withdraw(id, request);
    }

    @PostMapping("/transfer")
    public Account transferMoney(@Valid @RequestBody TransferRequest request) {
        return accountService.transfer(request);
    }

    @GetMapping("/{id}/transactions")
    public List<AccountTransaction> getTransactionHistory(@PathVariable String id) {
        return accountService.getTransactions(id);
    }
}