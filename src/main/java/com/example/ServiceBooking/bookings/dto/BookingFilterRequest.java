package com.example.ServiceBooking.bookings.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record BookingFilterRequest(
        @Size(max = 100, message = "Customer name must not exceed 100 characters")
        String customerName,

        @Size(max = 100, message = "Provider name must not exceed 100 characters")
        String providerName,

        @Size(max = 100, message = "Service category must not exceed 100 characters")
        String serviceCategory,

        BookingStatus status,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate fromDate,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate toDate,

        @Pattern(regexp = "^(bookingDate|amount|status)?$", message = "Invalid sortBy value. Allowed: bookingDate, amount, status")
        String sortBy,

        @Pattern(regexp = "^(ASC|DESC)?$", message = "Invalid sortOrder value. Allowed: ASC, DESC")
        String sortOrder,

        @Size(max = 100, message = "City must not exceed 100 characters")
        String city
) {}

