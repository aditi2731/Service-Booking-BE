package com.example.ServiceBooking.analytics.dto;

public record CustomerTrendResponse(
        Long customerId,
        long totalBookings
) {}

