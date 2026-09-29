package com.example.bankapp.models;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record MoneyRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount
) {
}