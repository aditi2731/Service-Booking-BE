package com.example.ServiceBooking.ratings.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record SubmitReviewRequest(
        @NotNull(message = "Booking ID is required")
        @Positive(message = "Booking ID must be positive")
        Long bookingId,

        @NotBlank(message = "Comment is required")
        @Size(min = 10, max = 1000, message = "Comment must be between 10 and 1000 characters")
        String comment
) {}

