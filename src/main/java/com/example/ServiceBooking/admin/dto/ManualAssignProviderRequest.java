package com.example.ServiceBooking.admin.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ManualAssignProviderRequest(
        @NotNull(message = "Provider ID is required")
        @Positive(message = "Provider ID must be positive")
        Long providerId
) {}

