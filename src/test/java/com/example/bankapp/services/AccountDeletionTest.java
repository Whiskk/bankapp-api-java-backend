package com.example.bankapp.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.server.ResponseStatusException;

import com.example.bankapp.models.Account;
import com.example.bankapp.models.AccountType;
import com.example.bankapp.models.Customer;
import com.example.bankapp.repos.AccountRepository;
import com.example.bankapp.repos.AccountTransactionRepository;
import com.example.bankapp.repos.CustomerRepository;

class AccountDeletionTest {

    private AccountRepository accounts;
    private AccountTransactionRepository transactions;
    private CustomerRepository customers;
    private AccountService service;
    private Authentication owner;

    @BeforeEach
    void setUp() {
        accounts = mock(AccountRepository.class);
        transactions = mock(AccountTransactionRepository.class);
        customers = mock(CustomerRepository.class);
        service = new AccountService(accounts, transactions, customers);
        owner = new UsernamePasswordAuthenticationToken(
                User.withUsername("ada").password("").authorities("ROLE_USER").build(), null,
                List.of(() -> "ROLE_USER"));
        when(customers.findByUsername("ada"))
                .thenReturn(Optional.of(new Customer("customer-1", "Ada", "ada", "hash")));
    }

    @Test
    void deletesZeroBalanceAndItsTransactions() {
        when(accounts.findById("account-1")).thenReturn(Optional.of(account("0.00")));
        when(accounts.deleteByIdIfZeroBalance("account-1")).thenReturn(true);

        service.deleteAccount("account-1", owner);

        verify(accounts).deleteByIdIfZeroBalance("account-1");
        verify(transactions).deleteByAccountIdIn(List.of("account-1"));
    }

    @Test
    void rejectsNonzeroBalanceWithoutDeleting() {
        when(accounts.findById("account-1")).thenReturn(Optional.of(account("0.01")));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.deleteAccount("account-1", owner));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(accounts, never()).deleteByIdIfZeroBalance("account-1");
    }

    @Test
    void rejectsAnotherCustomersAccount() {
        when(accounts.findById("account-1")).thenReturn(Optional.of(
                new Account("account-1", "customer-2", AccountType.SAVINGS, BigDecimal.ZERO)));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.deleteAccount("account-1", owner));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verify(accounts, never()).deleteByIdIfZeroBalance("account-1");
    }

    @Test
    void refusesDeletionIfBalanceChangedBeforeConditionalDelete() {
        when(accounts.findById("account-1")).thenReturn(Optional.of(account("0")));
        when(accounts.deleteByIdIfZeroBalance("account-1")).thenReturn(false);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.deleteAccount("account-1", owner));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(transactions, never()).deleteByAccountIdIn(List.of("account-1"));
    }

    private Account account(String balance) {
        return new Account("account-1", "customer-1", AccountType.SAVINGS, new BigDecimal(balance));
    }
}