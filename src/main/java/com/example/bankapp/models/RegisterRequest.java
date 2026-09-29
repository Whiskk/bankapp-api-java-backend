package com.example.bankapp.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String name,
        @NotBlank String username,
        @NotBlank @Size(min = 8) String password
) {
}