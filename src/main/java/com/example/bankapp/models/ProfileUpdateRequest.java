package com.example.bankapp.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @NotBlank String name,
        @NotBlank String username,
        String currentPassword,
        @Size(min = 8) String newPassword
) {
}