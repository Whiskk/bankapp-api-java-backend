package com.example.bankapp.models;

public record LoginResponse(String token, Long customerId, String username) {
}