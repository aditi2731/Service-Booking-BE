package com.example.ServiceBooking.bookings.unifiedfiltering;

import com.example.ServiceBooking.auth.JwtUtil;
import com.example.ServiceBooking.auth.Role;
import com.example.ServiceBooking.bookings.dto.BookingFilterRequest;
import com.example.ServiceBooking.bookings.dto.BookingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Unified Booking Filter", description = "Dynamic search/filter/sort API for Admin/Provider/Customer dashboards")
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingFilterController {

    private final BookingFilterService filterService;
    private final JwtUtil jwtUtil;

    private Long userId() {
        return jwtUtil.getCurrentUserId();
    }

    private Role currentRole() {
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return Role.ADMIN;

        boolean isProvider = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_PROVIDER"));
        if (isProvider) return Role.PROVIDER;

        boolean isCustomer = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));
        if (isCustomer) return Role.CUSTOMER;

        log.warn("Caller role could not be resolved");
        throw new RuntimeException("Not allowed");
    }

    @Operation(summary = "Unified dynamic filter + sorting + pagination for bookings")
    @PostMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','CUSTOMER')")
    public Page<BookingResponse> filter(
            @Valid @RequestBody(required = false) BookingFilterRequest request,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        log.info("Unified booking filter API called");
        log.debug("Incoming filter payload received (IDs not logged)");

        // If body missing -> treat as all missing -> return all visible bookings for role
        BookingFilterRequest safe = (request == null)
                ? new BookingFilterRequest(null, null, null, null, null, null, null, null, null)
                : request;

        return filterService.filterBookings(userId(), currentRole(), safe, pageable);
    }

    // Optional helper GET (nice for dashboards / quick sanity)
    @Operation(summary = "Quick default listing (same visibility rules) - optional")
    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','CUSTOMER')")
    public Page<BookingResponse> defaultFilter(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        log.info("Default booking filter GET called");
        BookingFilterRequest empty = new BookingFilterRequest(null, null, null, null, null, null, null, null, null);
        return filterService.filterBookings(userId(), currentRole(), empty, pageable);
    }
}
