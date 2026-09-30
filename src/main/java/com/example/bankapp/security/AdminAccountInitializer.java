package com.example.bankapp.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.bankapp.models.Customer;
import com.example.bankapp.repos.CustomerRepository;

@Component
public class AdminAccountInitializer implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public AdminAccountInitializer(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.username:}") String username,
            @Value("${app.admin.password:}") String password) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (username.isBlank() && password.isBlank()) {
            return;
        }
        if (username.isBlank() || password.isBlank()) {
            throw new IllegalStateException("Both ADMIN_USERNAME and ADMIN_PASSWORD must be set");
        }

        Customer existing = customerRepository.findByUsername(username).orElse(null);
        if (existing != null && !existing.admin()) {
            throw new IllegalStateException("ADMIN_USERNAME is already used by a customer account");
        }

        customerRepository.save(new Customer(
                existing == null ? null : existing.id(),
                existing == null ? "Administrator" : existing.name(),
                username,
                passwordEncoder.encode(password),
                true));
    }
}