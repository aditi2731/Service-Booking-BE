package com.example.ServiceBooking.analytics.dto;

import java.math.BigDecimal;

public record RevenueSummaryResponse(
        BigDecimal grossRevenue,
        BigDecimal platformCommission,
        BigDecimal providerNet
) {}

