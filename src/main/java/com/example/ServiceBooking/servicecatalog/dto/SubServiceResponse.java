package com.example.ServiceBooking.servicecatalog.dto;

import java.math.BigDecimal;

public record SubServiceResponse(
        Long id,
        String name,
        String description,
        BigDecimal basePrice,
        Long categoryId
) {}

