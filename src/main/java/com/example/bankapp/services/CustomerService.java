package com.example.bankapp.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import com.example.bankapp.models.Customer;
import com.example.bankapp.models.CustomerRequest;
import com.example.bankapp.repos.CustomerRepository;

import com.example.bankapp.models.Account;
import com.example.bankapp.repos.AccountRepository;
import com.example.bankapp.repos.AccountTransactionRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final AccountTransactionRepository accountTransactionRepository;

    public CustomerService(
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            AccountTransactionRepository accountTransactionRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.accountTransactionRepository = accountTransactionRepository;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(String id) {
        return findCustomer(id);
    }

    public Customer createCustomer(CustomerRequest request) {
        return customerRepository.save(new Customer(null, request.name(), null, null));
    }

    public Customer updateCustomer(String id, CustomerRequest request) {
        Customer customer = findCustomer(id);
        return customerRepository.save(new Customer(
            id, request.name(), customer.username(), customer.passwordHash()));
    }

    @Transactional
    public void deleteCustomer(String id) {
        findCustomer(id);

        List<Account> accounts = accountRepository.findByUserId(id);
        List<String> accountIds = accounts.stream()
                .map(Account::id) // Use Account::getId if Account is a standard class with getters
                .toList();

        if (!accountIds.isEmpty()) {
            accountTransactionRepository.deleteByAccountIdIn(accountIds);
        }

        accountRepository.deleteByUserId(id);
        customerRepository.deleteById(id);
    }

    private Customer findCustomer(String id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> notFound(id));
    }

    private ResponseStatusException notFound(String id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer %s was not found".formatted(id));
    }
}