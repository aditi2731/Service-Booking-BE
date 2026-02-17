package com.example.ServiceBooking.admin.dto;


import java.math.BigDecimal;

public record ProviderPerformanceResponse(
        Long providerId,
        long completedBookings,
        BigDecimal grossEarnings,
        BigDecimal platformCommission,
        BigDecimal providerNet
) {}

