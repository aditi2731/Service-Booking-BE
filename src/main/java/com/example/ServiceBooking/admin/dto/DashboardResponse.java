package com.example.ServiceBooking.admin.dto;



import java.math.BigDecimal;

public record DashboardResponse(
        long totalUsers,
        long totalCustomers,
        long totalProviders,
        long totalBookings,
        long totalCompletedBookings,
        long totalPaymentsPaid,
        BigDecimal totalGrossRevenue,
        BigDecimal totalPlatformCommission,
        BigDecimal totalProviderNet
) {}

