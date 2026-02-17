package com.example.ServiceBooking.analytics;

import com.example.ServiceBooking.analytics.dto.MonthlyBookingReportResponse;
import com.example.ServiceBooking.analytics.dto.RevenueSummaryResponse;
import com.example.ServiceBooking.analytics.dto.ProviderActivityResponse;
import com.example.ServiceBooking.analytics.dto.CustomerTrendResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Reporting & Analytics Service", description = "Analytics and reporting APIs")
@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final AnalyticsService service;

    @Operation(summary = "Monthly booking report-ADMIN")
    @GetMapping("/bookings/monthly")
    @PreAuthorize("hasRole('ADMIN')")
    public MonthlyBookingReportResponse monthlyReport(
            @RequestParam int year,
            @RequestParam int month) {

        log.trace("Entering monthlyReport method");
        log.debug("Processing monthly booking report request");
        log.info("Monthly booking report requested");
        try {
            MonthlyBookingReportResponse response = service.monthlyBookingReport(year, month);
            log.debug("Monthly booking report generated successfully");
            return response;
        } catch (Exception e) {
            log.error("Error generating monthly booking report");
            throw e;
        }
    }

    @Operation(summary = "Revenue summary-ADMIN")
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public RevenueSummaryResponse revenueSummary() {
        log.trace("Entering revenueSummary method");
        log.debug("Processing revenue summary request");
        log.info("Revenue summary requested");
        try {
            RevenueSummaryResponse response = service.revenueSummary();
            log.debug("Revenue summary generated successfully");
            return response;
        } catch (Exception e) {
            log.error("Error generating revenue summary");
            throw e;
        }
    }

    @Operation(summary = "Provider activity analytics-ADMIN")
    @GetMapping("/providers")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<ProviderActivityResponse> providerAnalytics(
            @PageableDefault(size = 20) Pageable pageable) {

        log.trace("Entering providerAnalytics method");
        log.debug("Processing provider analytics request");
        log.info("Provider analytics requested");
        try {
            Page<ProviderActivityResponse> response = service.providerAnalytics(pageable);
            log.debug("Provider analytics generated successfully");
            return response;
        } catch (Exception e) {
            log.error("Error generating provider analytics");
            throw e;
        }
    }

    @Operation(summary = "Customer usage trends -ADMIN")
    @GetMapping("/customers")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<CustomerTrendResponse> customerTrends(
            @PageableDefault(size = 20) Pageable pageable) {

        log.trace("Entering customerTrends method");
        log.debug("Processing customer trends request");
        log.info("Customer usage trends requested");
        try {
            Page<CustomerTrendResponse> response = service.customerUsage(pageable);
            log.debug("Customer trends generated successfully");
            return response;
        } catch (Exception e) {
            log.error("Error generating customer trends");
            throw e;
        }
    }
}

