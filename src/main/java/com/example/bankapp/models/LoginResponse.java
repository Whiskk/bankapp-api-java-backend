package com.example.bankapp.models;

public record LoginResponse(String token, String customerId, String username, boolean admin) {
}