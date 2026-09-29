package com.example.bankapp.repos;

import java.util.List;
import java.util.Optional;

import com.example.bankapp.models.Customer;

public interface CustomerRepository {

    List<Customer> findAll();

    Optional<Customer> findById(Long id);

    Customer save(Customer customer);

    boolean deleteById(Long id);
}