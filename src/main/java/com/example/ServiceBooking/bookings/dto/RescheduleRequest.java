package com.example.ServiceBooking.bookings.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record RescheduleRequest(

        @NotNull(message = "New date and time is required")
        @Future(message = "New booking date must be in the future")
        LocalDateTime newDateTime,

        // optional — if null, existing location is kept unchanged
        @Size(min = 3, max = 255, message = "Location must be between 5 and 255 characters")
        String location
) {}
