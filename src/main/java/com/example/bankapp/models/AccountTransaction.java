package com.example.bankapp.models;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountTransaction(
        Long id,
        Long accountId,
        TransactionType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        Instant createdAt
) {
}