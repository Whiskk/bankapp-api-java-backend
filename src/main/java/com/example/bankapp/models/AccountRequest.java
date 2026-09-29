package com.example.bankapp.models;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AccountRequest(
        @NotNull @Positive Long userId,
        @NotNull AccountType accountType
) {
}