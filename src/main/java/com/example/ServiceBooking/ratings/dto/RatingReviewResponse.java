package com.example.ServiceBooking.ratings.dto;


import java.time.LocalDateTime;

public record RatingReviewResponse(
        Long ratingId,
        Long bookingId,
        Long providerId,
        Long serviceId,
        Integer stars,
        String comment,
        LocalDateTime createdAt
) {}

