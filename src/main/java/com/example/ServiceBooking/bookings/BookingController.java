package com.example.ServiceBooking.bookings;

import com.example.ServiceBooking.auth.JwtUtil;
import com.example.ServiceBooking.bookings.dto.BookingCreateRequest;
import com.example.ServiceBooking.bookings.dto.BookingResponse;
import com.example.ServiceBooking.bookings.dto.SlotBookingRequest;
import com.example.ServiceBooking.bookings.dto.SlotResponse;
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

    @Operation(summary = "Create a new booking- CUSTOMER")
    // CUSTOMER
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public void create(@Valid @RequestBody BookingCreateRequest request) {
        log.trace("Entering create method");
        log.debug("Processing create booking request");
        log.info("Create booking requested");
        try {
            service.createBooking(userId(), request);
            log.debug("Booking created successfully");
        } catch (Exception e) {
            log.error("Error creating booking");
            throw e;
        }
    }

    @Operation(summary = "Cancel a booking - CUSTOMER/PROVIDER")
    // CUSTOMER / PROVIDER
    @PostMapping("/{id}/cancel")
    public void cancel(@PathVariable Long id) {
        log.trace("Entering cancel method");
        log.debug("Processing cancel booking request");
        log.info("Cancel booking requested");
        try {
            service.cancelBooking(id, userId());
            log.debug("Booking cancelled successfully");
        } catch (Exception e) {
            log.error("Error cancelling booking");
            throw e;
        }
    }

    @Operation(summary = "Get current bookings - CUSTOMER/PROVIDER")
    // CUSTOMER
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/customer")
    public List<BookingResponse> customerBookings() {
        log.trace("Entering customerBookings method");
        log.debug("Processing get customer bookings request");
        log.info("Get customer bookings requested");
        try {
            List<BookingResponse> bookings = service.customerBookings(userId());
            log.debug("Customer bookings retrieved successfully");
            return bookings;
        } catch (Exception e) {
            log.error("Error retrieving customer bookings");
            throw e;
        }
    }

    @Operation(summary = "Get current bookings - PROVIDER")
    // PROVIDER
    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/provider")
    public List<BookingResponse> providerBookings() {
        log.trace("Entering providerBookings method");
        log.debug("Processing get provider bookings request");
        log.info("Get provider bookings requested");
        try {
            List<BookingResponse> bookings = service.providerBookings(userId());
            log.debug("Provider bookings retrieved successfully");
            return bookings;
        } catch (Exception e) {
            log.error("Error retrieving provider bookings");
            throw e;
        }
    }

    @Operation(summary = "Get booking history - CUSTOMER")
    // HISTORY
    @GetMapping("/history")
    public List<BookingResponse> history() {
        log.trace("Entering history method");
        log.debug("Processing get booking history request");
        log.info("Get booking history requested");
        try {
//        boolean isCustomer = JwtUtil.hasRole("CUSTOMER");
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            boolean isCustomer = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));

            List<BookingResponse> history = service.history(userId(), isCustomer);
            log.debug("Booking history retrieved successfully");
            return history;
        } catch (Exception e) {
            log.error("Error retrieving booking history");
            throw e;
        }
    }

    //slots booking
    @Operation(summary = "Get available slots for a service and date")
    @GetMapping("/slots")
    public List<SlotResponse> slots(@RequestParam Long serviceId,
                                    @RequestParam LocalDate date) {
        return service.getSlotsForService(serviceId, date);
    }

    @Operation(summary = "Book a slot - CUSTOMER")
    @PostMapping("/slot")
    @PreAuthorize("hasRole('CUSTOMER')")
    public BookingResponse bookSlot(@RequestBody @jakarta.validation.Valid SlotBookingRequest req) {
        return service.bookSlot(userId(), req);
    }

}

