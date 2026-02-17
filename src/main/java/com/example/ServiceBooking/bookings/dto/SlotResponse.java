package com.example.ServiceBooking.bookings.dto;

import java.time.LocalTime;

public record SlotResponse(
        LocalTime startTime,
        LocalTime endTime,
        long availableProviders
) {}
