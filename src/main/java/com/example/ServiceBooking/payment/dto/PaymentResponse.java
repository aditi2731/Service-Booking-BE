package com.example.ServiceBooking.payment.dto;

import com.example.ServiceBooking.payment.PaymentMethod;
import com.example.ServiceBooking.payment.PaymentStatus;

import java.math.BigDecimal;

public record PaymentResponse(
        Long paymentId,
        Long bookingId,
        BigDecimal amount,          // gross
        PaymentMethod method,
        PaymentStatus status,
        BigDecimal providerReceives, // net (computed)
        BigDecimal platformCommission // computed
) {}
