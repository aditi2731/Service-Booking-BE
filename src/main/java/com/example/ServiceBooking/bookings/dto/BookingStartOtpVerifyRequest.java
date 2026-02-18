package com.example.ServiceBooking.bookings.dto;

import jakarta.validation.constraints.NotBlank;

public record BookingStartOtpVerifyRequest(
        @NotBlank(message = "OTP is required")
        String otp
) {}
