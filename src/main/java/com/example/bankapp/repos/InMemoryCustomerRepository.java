package com.example.bankapp.repos;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.UUID;

import com.example.bankapp.models.Customer;

public class InMemoryCustomerRepository implements CustomerRepository {

    private final ConcurrentMap<String, Customer> customers = new ConcurrentHashMap<>();

    @Override
    public List<Customer> findAll() {
        return customers.values().stream().toList();
    }

    @Override
    public java.util.Optional<Customer> findById(String id) {
        return java.util.Optional.ofNullable(customers.get(id));
    }

    @Override
    public Optional<Customer> findByUsername(String username) {
        return customers.values().stream()
                .filter(customer -> username.equalsIgnoreCase(customer.username()))
                .findFirst();
    }

    @Override
    public Customer save(Customer customer) {
        String id = customer.id() == null ? UUID.randomUUID().toString() : customer.id();
        Customer savedCustomer = new Customer(id, customer.name(), customer.username(), customer.passwordHash());
        customers.put(id, savedCustomer);
        return savedCustomer;
    }

    @Override
    public boolean deleteById(String id) {
        return customers.remove(id) != null;
    }
}