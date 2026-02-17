package com.example.ServiceBooking.bookings.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record SlotBookingRequest(
        @NotNull Long serviceId,
        Long providerId, // optional: if null -> system assigns
        @NotNull LocalDateTime dateTime,
        @NotNull String location
) {}
