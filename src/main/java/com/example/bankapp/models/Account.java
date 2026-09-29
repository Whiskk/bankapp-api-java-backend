package com.example.bankapp.models;

import java.math.BigDecimal;

public record Account(Long id, Long userId, AccountType accountType, BigDecimal balance) {
}