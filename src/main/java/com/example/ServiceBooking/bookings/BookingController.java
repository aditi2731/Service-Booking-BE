package com.example.ServiceBooking.bookings;

import com.example.ServiceBooking.auth.JwtUtil;
import com.example.ServiceBooking.bookings.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Tag(name = "Bookings", description = "Endpoints for managing service bookings")
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService service;

    private Long userId() {
        return JwtUtil.getCurrentUserId();
    }

    @Operation(summary = "Create a new booking - CUSTOMER")
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public void create(@Valid @RequestBody BookingCreateRequest request) {
        service.createBooking(userId(), request);
    }

    @Operation(summary = "Cancel a booking - CUSTOMER/PROVIDER")
    @PostMapping("/{id}/cancel")
    public void cancel(@PathVariable Long id) {
        service.cancelBooking(id, userId());
    }

    @Operation(summary = "Get current bookings - CUSTOMER")
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/customer")
    public List<BookingResponse> customerBookings() {
        return service.customerBookings(userId());
    }

    @Operation(summary = "Get current bookings - PROVIDER")
    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/provider")
    public List<BookingResponse> providerBookings() {
        return service.providerBookings(userId());
    }

    @Operation(summary = "Get booking history - CUSTOMER/PROVIDER")
    @GetMapping("/history")
    public List<BookingResponse> history() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isCustomer = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));
        return service.history(userId(), isCustomer);
    }

    // =========================
    // SLOTS (EXISTING)
    // =========================
    @Operation(summary = "Get available slots for a service and date")
    @GetMapping("/slots")
    public List<SlotResponse> slots(@RequestParam Long serviceId,
                                    @RequestParam LocalDate date,
                                    @RequestParam String city) {
        return service.getSlotsForService(serviceId, date, city);
    }

    @Operation(summary = "Book a slot - CUSTOMER")
    @PostMapping("/slot")
    @PreAuthorize("hasRole('CUSTOMER')")
    public BookingResponse bookSlot(@RequestBody @Valid SlotBookingRequest req) {
        return service.bookSlot(userId(), req);
    }

    // ======================================================
    // JOB START OTP FLOW
    // ======================================================

    @Operation(summary = "Verify Start OTP before starting job - PROVIDER")
    @PostMapping("/{id}/start/verify-otp")
    @PreAuthorize("hasRole('PROVIDER')")
    public void verifyStartOtp(@PathVariable Long id,
                               @RequestBody @Valid BookingStartOtpVerifyRequest req) {
        service.verifyStartOtp(id, userId(), req.otp());
    }

    @Operation(summary = "Resend Start OTP - CUSTOMER")
    @PostMapping("/{id}/start/resend-otp")
    @PreAuthorize("hasRole('CUSTOMER')")
    public void resendStartOtp(@PathVariable Long id) {
        service.resendStartOtp(id, userId());
    }

    // ======================================================
    // Nearby Providers
    // ======================================================

    @Operation(summary = "Get nearby providers in customer's city (optional service filter)")
    @GetMapping("/providers/nearby")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<NearbyProviderResponse> nearbyProviders(
            @RequestParam(required = false) Long serviceId
    ) {
        return service.getNearbyProviders(userId(), serviceId);
    }
}
