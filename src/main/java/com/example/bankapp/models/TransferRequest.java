package com.example.bankapp.models;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransferRequest(
    @NotNull
    String fromAccountId,

    @NotNull
    String toAccountId,

    @NotNull
    @Positive
    @DecimalMin("0.01")
    BigDecimal amount
) {
    
}
