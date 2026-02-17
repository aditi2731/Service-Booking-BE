package com.example.ServiceBooking.servicecatalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SubServiceRequest(
        @NotBlank(message = "Sub-service name is required")
        @Size(min = 2, max = 100, message = "Sub-service name must be between 2 and 100 characters")
        String name,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @NotNull(message = "Base price is required")
        @DecimalMin(value = "0.01", message = "Base price must be greater than zero")
        BigDecimal basePrice
) {}

