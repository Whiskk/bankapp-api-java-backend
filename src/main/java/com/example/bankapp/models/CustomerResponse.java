package com.example.bankapp.models;

public record CustomerResponse(Long id, String name, String username) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(customer.id(), customer.name(), customer.username());
    }
}