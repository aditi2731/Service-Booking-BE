package com.example.ServiceBooking.ratings.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SubmitRatingRequest(
        @NotNull(message = "Booking ID is required")
        @Positive(message = "Booking ID must be positive")
        Long bookingId,

        @NotNull(message = "Stars rating is required")
        @Min(value = 1, message = "Stars must be at least 1")
        @Max(value = 5, message = "Stars must be at most 5")
        Integer stars
) {}

