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
        log.trace("Entering create method");
        log.info("Creating new booking");
        service.createBooking(userId(), request);
        log.debug("Booking created successfully");
    }

    @Operation(summary = "Cancel a booking - CUSTOMER/PROVIDER")
    @PostMapping("/{id}/cancel")
    public void cancel(@PathVariable Long id) {
        log.trace("Entering cancel method");
        log.info("Cancelling booking");
        service.cancelBooking(id, userId());
        log.debug("Booking cancelled successfully");
    }

    @Operation(summary = "Get current bookings - CUSTOMER")
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/customer")
    public List<BookingResponse> customerBookings() {
        log.trace("Entering customerBookings method");
        log.info("Fetching customer bookings");
        List<BookingResponse> bookings = service.customerBookings(userId());
        log.debug("Customer bookings retrieved successfully");
        return bookings;
    }

    @Operation(summary = "Get current bookings - PROVIDER")
    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/provider")
    public List<BookingResponse> providerBookings() {
        log.trace("Entering providerBookings method");
        log.info("Fetching provider bookings");
        List<BookingResponse> bookings = service.providerBookings(userId());
        log.debug("Provider bookings retrieved successfully");
        return bookings;
    }

    @Operation(summary = "Get booking history - CUSTOMER/PROVIDER")
    @GetMapping("/history")
    public List<BookingResponse> history() {
        log.trace("Entering history method");
        log.info("Fetching booking history");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isCustomer = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));
        List<BookingResponse> history = service.history(userId(), isCustomer);
        log.debug("Booking history retrieved successfully");
        return history;
    }

    // =========================
    // SLOTS (EXISTING)
    // =========================
    @Operation(summary = "Get available slots for a service and date")
    @GetMapping("/slots")
    public List<SlotResponse> slots(@RequestParam Long serviceId,
                                    @RequestParam LocalDate date,
                                    @RequestParam String city) {
        log.trace("Entering slots method");
        log.info("Fetching available slots");
        List<SlotResponse> slots = service.getSlotsForService(serviceId, date, city);
        log.debug("Slots retrieved successfully");
        return slots;
    }

    @Operation(summary = "Book a slot - CUSTOMER")
    @PostMapping("/slot")
    @PreAuthorize("hasRole('CUSTOMER')")
    public BookingResponse bookSlot(@RequestBody @Valid SlotBookingRequest req) {
        log.trace("Entering bookSlot method");
        log.info("Booking slot");
        BookingResponse response = service.bookSlot(userId(), req);
        log.debug("Slot booked successfully");
        return response;
    }

    // ======================================================
    // JOB START OTP FLOW
    // ======================================================

    @Operation(summary = "Verify Start OTP before starting job - PROVIDER")
    @PostMapping("/{id}/start/verify-otp")
    @PreAuthorize("hasRole('PROVIDER')")
    public void verifyStartOtp(@PathVariable Long id,
                               @RequestBody @Valid BookingStartOtpVerifyRequest req) {
        log.trace("Entering verifyStartOtp method");
        log.info("Verifying start OTP");
        service.verifyStartOtp(id, userId(), req.otp());
        log.debug("Start OTP verified successfully");
    }

    @Operation(summary = "Resend Start OTP - CUSTOMER")
    @PostMapping("/{id}/start/resend-otp")
    @PreAuthorize("hasRole('CUSTOMER')")
    public void resendStartOtp(@PathVariable Long id) {
        log.trace("Entering resendStartOtp method");
        log.info("Resending start OTP");
        service.resendStartOtp(id, userId());
        log.debug("Start OTP resent successfully");
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
        log.trace("Entering nearbyProviders method");
        log.info("Fetching nearby providers");
        List<NearbyProviderResponse> providers = service.getNearbyProviders(userId(), serviceId);
        log.debug("Nearby providers retrieved successfully");
        return providers;
    }
}
