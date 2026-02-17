package com.example.ServiceBooking.payment;

import com.example.ServiceBooking.auth.JwtUtil;
import com.example.ServiceBooking.payment.dto.PaymentResponse;
import com.example.ServiceBooking.payment.dto.RecordCashPaymentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Payment Service", description = "Cash payment records + commission + history")
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final JwtUtil jwtUtil;

    private Long userId() {
        return jwtUtil.getCurrentUserId();
    }

    @Operation(summary = "Record CASH payment - PROVIDER")
    @PostMapping("/cash")
    @PreAuthorize("hasRole('PROVIDER')")
    public PaymentResponse recordCash(@Valid @RequestBody RecordCashPaymentRequest req) {
        log.info("Record cash payment request received");
        return paymentService.recordCashPayment(userId(), req);
    }

    @Operation(summary = "Mark payment PAID after service completion - PROVIDER")
    @PutMapping("/{paymentId}/paid")
    @PreAuthorize("hasRole('PROVIDER')")
    public PaymentResponse markPaid(@PathVariable Long paymentId) {
        log.info("Mark payment paid request received");
        return paymentService.markPaymentPaid(userId(), paymentId);
    }

    @Operation(summary = "Payment history - ADMIN")
    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<PaymentResponse> history(
            @PageableDefault(size = 20, sort = "paymentId") Pageable pageable
    ) {
        log.info("Payment history request received");
        return paymentService.paymentHistory(pageable);
    }
}
