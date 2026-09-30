package com.example.bankapp.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.bankapp.models.Customer;
import com.example.bankapp.models.CustomerRequest;
import com.example.bankapp.repos.CustomerRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
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

    public void deleteCustomer(String id) {
        if (!customerRepository.deleteById(id)) {
            throw notFound(id);
        }
    }

    private Customer findCustomer(String id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> notFound(id));
    }

    private ResponseStatusException notFound(String id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer %s was not found".formatted(id));
    }
}