package com.example.ServiceBooking.analytics.dto;

public record ProviderActivityResponse(
        Long providerId,
        long completedBookings
) {}

