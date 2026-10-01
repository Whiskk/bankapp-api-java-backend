package com.example.bankapp.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import com.example.bankapp.models.Customer;
import com.example.bankapp.models.CustomerRequest;
import com.example.bankapp.models.ProfileUpdateRequest;
import com.example.bankapp.repos.CustomerRepository;

import com.example.bankapp.models.Account;
import com.example.bankapp.repos.AccountRepository;
import com.example.bankapp.repos.AccountTransactionRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final AccountTransactionRepository accountTransactionRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            AccountTransactionRepository accountTransactionRepository,
            PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.accountTransactionRepository = accountTransactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll().stream()
            .filter(customer -> !customer.admin())
            .toList();
    }

    public Customer getCustomerById(String id) {
        return findCustomer(id);
    }

    public Customer getCustomerByUsername(String username) {
        return customerRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    public Customer updateOwnProfile(String username, ProfileUpdateRequest request) {
        Customer customer = getCustomerByUsername(username);
        String requestedUsername = request.username().trim();
        boolean changingUsername = !customer.username().equals(requestedUsername);
        boolean changingPassword = request.newPassword() != null;

        if ((changingUsername || changingPassword)
                && (request.currentPassword() == null
                    || !passwordEncoder.matches(request.currentPassword(), customer.passwordHash()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current password is incorrect");
        }
        if (changingUsername && customerRepository.findByUsername(requestedUsername).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already in use");
        }

        return customerRepository.save(new Customer(
                customer.id(),
                request.name().trim(),
                requestedUsername,
                changingPassword ? passwordEncoder.encode(request.newPassword()) : customer.passwordHash(),
                customer.admin()));
    }

    public Customer createCustomer(CustomerRequest request) {
        return customerRepository.save(new Customer(null, request.name(), null, null));
    }

    public Customer updateCustomer(String id, CustomerRequest request) {
        Customer customer = findCustomer(id);
        return customerRepository.save(new Customer(
            id, request.name(), customer.username(), customer.passwordHash(), customer.admin()));
    }

    @Transactional
    public void deleteCustomer(String id) {
        Customer customer = findCustomer(id);
        if (customer.admin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "The admin account cannot be deleted");
        }

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