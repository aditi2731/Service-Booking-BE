package com.example.ServiceBooking.analytics;

import com.example.ServiceBooking.analytics.dto.*;
import com.example.ServiceBooking.bookings.BookingRepository;
import com.example.ServiceBooking.bookings.dto.BookingStatus;
import com.example.ServiceBooking.payment.PaymentRepository;
import com.example.ServiceBooking.payment.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final BookingRepository bookingRepo;
    private final PaymentRepository paymentRepo;

    @Value("${platform.commission.rate:0.20}")
    private BigDecimal commissionRate;

    public MonthlyBookingReportResponse monthlyBookingReport(int year, int month) {
        log.trace("Entering monthlyBookingReport method");
        log.info("Generating monthly booking report");
        log.debug("Calculating monthly booking report");

        YearMonth ym = YearMonth.of(year, month);

        long total = bookingRepo.countByMonth(year, month);
        long completed = bookingRepo.countByMonthAndStatus(year, month, BookingStatus.COMPLETED);
        long cancelled = bookingRepo.countByMonthAndStatus(year, month, BookingStatus.CANCELLED);

        if (total == 0) {
            log.warn("No bookings found for the specified month");
        }

        log.debug("Monthly booking report calculated successfully");
        return new MonthlyBookingReportResponse(
                ym.toString(),
                total,
                completed,
                cancelled
        );
    }

    public RevenueSummaryResponse revenueSummary() {
        log.trace("Entering revenueSummary method");
        log.info("Generating revenue summary");
        log.debug("Calculating revenue summary");

        BigDecimal gross = paymentRepo.sumByStatus(PaymentStatus.PAID);
        if (gross == null) {
            log.warn("No paid payments found, setting gross to zero");
            gross = BigDecimal.ZERO;
        }

        BigDecimal commission = gross.multiply(commissionRate);
        BigDecimal net = gross.subtract(commission);

        log.debug("Revenue summary calculated successfully");
        return new RevenueSummaryResponse(gross, commission, net);
    }

    public Page<ProviderActivityResponse> providerAnalytics(Pageable pageable) {
        log.trace("Entering providerAnalytics method");
        log.info("Generating provider activity analytics");
        log.debug("Fetching provider activity analytics");

        Page<ProviderActivityResponse> result = bookingRepo.providerActivity(pageable)
                .map(p -> new ProviderActivityResponse(
                        p.getProviderId(),
                        p.getCompletedBookings()
                ));

        log.debug("Provider activity analytics fetched successfully");
        return result;
    }

    public Page<CustomerTrendResponse> customerUsage(Pageable pageable) {
        log.trace("Entering customerUsage method");
        log.info("Generating customer usage trends");
        log.debug("Fetching customer usage trends");

        Page<CustomerTrendResponse> result = bookingRepo.customerUsage(pageable)
                .map(c -> new CustomerTrendResponse(
                        c.getCustomerId(),
                        c.getTotalBookings()
                ));

        log.debug("Customer usage trends fetched successfully");
        return result;
    }
}

