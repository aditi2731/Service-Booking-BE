package com.example.ServiceBooking.admin.dto;



public record BookingReportResponse(
        long totalBookings,
        long completedBookings,
        long cancelledBookings
) {}

