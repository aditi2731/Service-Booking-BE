package com.example.ServiceBooking.ratings.dto;



public record ProviderRatingSummaryResponse(
        Long providerId,
        Double averageStars,
        Long totalRatings
) {}

