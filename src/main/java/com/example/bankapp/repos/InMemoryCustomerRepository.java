package com.example.bankapp.repos;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.example.bankapp.models.Customer;

@Repository
public class InMemoryCustomerRepository implements CustomerRepository {

    private final AtomicLong nextId = new AtomicLong(1);
    private final ConcurrentMap<Long, Customer> customers = new ConcurrentHashMap<>();

    @Override
    public List<Customer> findAll() {
        return customers.values().stream().toList();
    }

    @Override
    public java.util.Optional<Customer> findById(Long id) {
        return java.util.Optional.ofNullable(customers.get(id));
    }

    @Override
    public Customer save(Customer customer) {
        Long id = customer.id() == null ? nextId.getAndIncrement() : customer.id();
        Customer savedCustomer = new Customer(id, customer.name());
        customers.put(id, savedCustomer);
        return savedCustomer;
    }

    @Override
    public boolean deleteById(Long id) {
        return customers.remove(id) != null;
    }
}