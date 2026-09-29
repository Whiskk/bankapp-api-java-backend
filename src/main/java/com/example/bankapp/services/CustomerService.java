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

    public Customer getCustomerById(Long id) {
        return findCustomer(id);
    }

    public Customer createCustomer(CustomerRequest request) {
        return customerRepository.save(new Customer(null, request.name()));
    }

    public Customer updateCustomer(Long id, CustomerRequest request) {
        findCustomer(id);
        return customerRepository.save(new Customer(id, request.name()));
    }

    public void deleteCustomer(Long id) {
        if (!customerRepository.deleteById(id)) {
            throw notFound(id);
        }
    }

    private Customer findCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> notFound(id));
    }

    private ResponseStatusException notFound(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer %d was not found".formatted(id));
    }
}