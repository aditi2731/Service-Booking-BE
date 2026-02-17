package com.example.ServiceBooking.customer.dto;

public record AddressResponse(
        Long id,
        String addressLine,
        String city,
        String state,
        String pincode,
        boolean DefaultAddress
) {}

