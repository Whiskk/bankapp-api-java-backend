package com.example.bankapp.services;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.bankapp.models.Customer;
import com.example.bankapp.models.CustomerResponse;
import com.example.bankapp.models.LoginRequest;
import com.example.bankapp.models.LoginResponse;
import com.example.bankapp.models.RegisterRequest;
import com.example.bankapp.repos.CustomerRepository;
import com.example.bankapp.security.JwtService;

@Service
public class AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public CustomerResponse register(RegisterRequest request) {
        if (customerRepository.findByUsername(request.username()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already in use");
        }

        Customer customer = customerRepository.save(new Customer(
                null,
                request.name(),
                request.username(),
                passwordEncoder.encode(request.password())));
        return CustomerResponse.from(customer);
    }

    public LoginResponse login(LoginRequest request) {
        Customer customer = customerRepository.findByUsername(request.username())
                .orElseThrow(this::invalidCredentials);

        if (!passwordEncoder.matches(request.password(), customer.passwordHash())) {
            throw invalidCredentials();
        }

        return new LoginResponse(
            jwtService.createToken(customer.id()),
                customer.id(),
                customer.username(),
                customer.admin());
    }

    public LoginResponse adminLogin(LoginRequest request) {
        LoginResponse response = login(request);
        if (!response.admin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
        return response;
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }
}