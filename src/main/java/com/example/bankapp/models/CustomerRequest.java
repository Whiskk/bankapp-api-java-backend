package com.example.bankapp.models;

import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(
        @NotBlank(message = "Name is required") String name
) {
}