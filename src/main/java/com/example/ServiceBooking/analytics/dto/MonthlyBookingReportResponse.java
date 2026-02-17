package com.example.ServiceBooking.analytics.dto;

public record MonthlyBookingReportResponse(
        String month,
        long totalBookings,
        long completedBookings,
        long cancelledBookings
) {}
