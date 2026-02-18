package com.example.ServiceBooking.bookings.dto;

import java.time.LocalDateTime;

public record BookingResponse(
        Long bookingId,
        Long customerId,
        Long providerId,
        Long serviceId,
        LocalDateTime dateTime,
        String location,
        String city,
        BookingStatus status
) {}

