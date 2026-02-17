package com.example.ServiceBooking.customer.dto;

import java.util.List;

public record CustomerProfileResponse(
        Long customerId,
        String name,
        String email,
        List<AddressResponse> addresses
) {}

