package com.example.ServiceBooking.bookings.dto;


public record NearbyProviderResponse(
        Long providerId,
        String providerName,
        String city
) {}
