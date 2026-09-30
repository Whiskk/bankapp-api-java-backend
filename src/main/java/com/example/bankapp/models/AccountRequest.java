package com.example.bankapp.models;

import jakarta.validation.constraints.NotNull;

public record AccountRequest(
        @NotNull String userId,
        @NotNull AccountType accountType
) {
}