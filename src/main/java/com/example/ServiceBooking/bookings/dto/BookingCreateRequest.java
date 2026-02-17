package com.example.ServiceBooking.bookings.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record BookingCreateRequest(
        @NotNull(message = "Service ID is required")
        Long serviceId,

        @NotNull(message = "Date and time is required")
        @Future(message = "Booking date must be in the future")
        LocalDateTime dateTime,

        @NotBlank(message = "Location is required")
        @Size(min = 5, max = 255, message = "Location must be between 5 and 255 characters")
        String location
) {}
