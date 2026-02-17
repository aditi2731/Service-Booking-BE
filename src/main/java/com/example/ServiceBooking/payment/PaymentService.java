package com.example.ServiceBooking.payment;

import com.example.ServiceBooking.bookings.Booking;
import com.example.ServiceBooking.bookings.BookingRepository;
import com.example.ServiceBooking.bookings.dto.BookingStatus;
import com.example.ServiceBooking.payment.dto.PaymentResponse;
import com.example.ServiceBooking.payment.dto.RecordCashPaymentRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final BookingRepository bookingRepo;

    @Value("${platform.commission.rate:0.20}")
    private BigDecimal commissionRate;

    /**
     * Record CASH payment for a booking.
     * Typically done by PROVIDER after collecting cash (or admin if needed).
     * Creates a single payment record per booking (enforced by unique constraint).
     */
    @Transactional
    public PaymentResponse recordCashPayment(Long callerId, RecordCashPaymentRequest req) {
        log.info("Recording cash payment");
        log.debug("Validating cash payment request (IDs not logged)");

        if (req.bookingId() == null) throw new RuntimeException("bookingId is required");
        if (req.amount() == null || req.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("amount must be > 0");
        }

        Booking booking = bookingRepo.findById(req.bookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Only allow recording if provider is assigned
        if (booking.getProviderId() == null) {
            throw new RuntimeException("Provider not assigned to this booking");
        }

        // Typically: provider who owns the booking can record
        if (!booking.getProviderId().equals(callerId)) {
            throw new RuntimeException("Not allowed");
        }

        // Create only once
        if (paymentRepo.findByBookingId(req.bookingId()).isPresent()) {
            throw new RuntimeException("Payment already recorded for this booking");
        }

        // Optional: if booking has price, validate mismatch
        if (booking.getPrice() != null && booking.getPrice().compareTo(req.amount()) != 0) {
            throw new RuntimeException("Amount mismatch. Expected: " + booking.getPrice());
        }

        Payment p = new Payment();
        p.setBookingId(req.bookingId());
        p.setAmount(req.amount());
        p.setMethod(PaymentMethod.CASH);
        p.setStatus(PaymentStatus.RECORDED);

        Payment saved = paymentRepo.save(p);
        log.info("Cash payment recorded");
        return toResponse(saved);
    }

    /**
     * Mark payment as PAID.
     * Must be allowed only after booking is COMPLETED.
     * Typically provider confirms after service completion.
     */
    @Transactional
    public PaymentResponse markPaymentPaid(Long callerId, Long paymentId) {
        log.info("Marking payment as PAID");
        log.debug("Validating mark paid request (IDs not logged)");

        Payment p = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        Booking booking = bookingRepo.findById(p.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getProviderId() == null) {
            throw new RuntimeException("Provider not assigned to this booking");
        }

        // Provider only (minimal requirement)
        if (!booking.getProviderId().equals(callerId)) {
            throw new RuntimeException("Not allowed");
        }

        // Must be completed first
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new RuntimeException("Payment can be marked PAID only after service COMPLETED");
        }

        if (p.getStatus() == PaymentStatus.PAID) {
            return toResponse(p);
        }

        p.setStatus(PaymentStatus.PAID);
        Payment saved = paymentRepo.save(p);

        log.info("Payment marked as PAID");
        return toResponse(saved);
    }

    /**
     * Payment History (Admin/global)
     * Minimal requirement: history.
     */
    public Page<PaymentResponse> paymentHistory(Pageable pageable) {
        log.info("Fetching payment history (paginated)");
        return paymentRepo.findAllByOrderByPaymentIdDesc(pageable).map(this::toResponse);
    }

    private PaymentResponse toResponse(Payment p) {
        BigDecimal commission = p.getAmount()
                .multiply(commissionRate)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal providerNet = p.getAmount()
                .subtract(commission)
                .setScale(2, RoundingMode.HALF_UP);

        return new PaymentResponse(
                p.getPaymentId(),
                p.getBookingId(),
                p.getAmount(),
                p.getMethod(),
                p.getStatus(),
                providerNet,
                commission
        );
    }
}
