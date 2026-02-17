package com.example.ServiceBooking.admin.dto;

import jakarta.validation.constraints.NotNull;

public record SuspendAccountRequest(
        @NotNull(message = "Suspended flag is required")
        Boolean suspended
) {}

