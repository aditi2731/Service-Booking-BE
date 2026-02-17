package com.example.ServiceBooking.payment.dto;

import jakarta.validation.constraints.Size;

public record MarkPaymentCompleteRequest(
        @Size(max = 100, message = "Transaction reference must not exceed 100 characters")
        String transactionRef // optional: allow completing by ref (useful for mock online callback)
) {}

