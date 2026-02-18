package com.example.ServiceBooking.admin;



import com.example.ServiceBooking.admin.dto.*;
import com.example.ServiceBooking.auth.User;
import com.example.ServiceBooking.servicecatalog.ServiceCategory;
import com.example.ServiceBooking.servicecatalog.SubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.security.authorization.AuthorityReactiveAuthorizationManager.hasRole;

@Tag(name = "Admin Management Service", description = "Admin dashboard, user controls, service controls, booking controls, reports")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminManagementController {

    private final AdminManagementService adminService;

    // ---------- DASHBOARD ----------
    @Operation(summary = "Dashboard metrics-ADMIN")
    @GetMapping("/dashboard")
    public DashboardResponse dashboard(@RequestParam(required = false) String city) {
        log.trace("Entering dashboard method");
        log.debug("Processing dashboard request");
        log.info("Dashboard API called");
        DashboardResponse response = adminService.dashboard(city);
        log.debug("Dashboard response generated successfully");
        return response;
    }

    // ---------- USER CONTROLS ----------
    @Operation(summary = "View customers (paginated)- ADMIN")
    @PreAuthorize("hasRole('ADMIN')") // Ensure only admins can access this endpoint
    @GetMapping("/users/customers")
    public Page<User> customers(@PageableDefault(size = 20) Pageable pageable) {
        log.trace("Entering customers method");
        log.debug("Fetching customers with pagination");
        log.info("View customers API called");
        Page<User> customers = adminService.viewCustomers(pageable);
        log.debug("Retrieved customers page successfully");
        return customers;
    }

    @Operation(summary = "Suspend/unsuspend a user account- ADMIN")
    @PreAuthorize("hasRole('ADMIN')") // Ensure only admins can access this endpoint
    @PutMapping("/users/{userId}/suspend")
    public void suspend(@PathVariable Long userId, @Valid @RequestBody SuspendAccountRequest req) {
        log.trace("Entering suspend method");
        log.debug("Processing suspend request for user");
        log.info("Suspend user API called");
        try {
            adminService.suspendAccount(userId, req.suspended());
            log.debug("Suspend operation completed successfully");
        } catch (Exception e) {
            log.error("Error during suspend operation");
            throw e;
        }
    }

    @Operation(summary = "Approve/reject a provider- ADMIN")
    @PreAuthorize("hasRole('ADMIN')") // Ensure only admins can access this endpoint
    @PutMapping("/providers/{providerId}/approve")
    public void approveProvider(@PathVariable Long providerId, @RequestParam boolean approved) {
        log.trace("Entering approveProvider method");
        log.debug("Processing provider approval request");
        log.info("Approve provider API called");
        try {
            adminService.approveProvider(providerId, approved);
            log.debug("Provider approval updated successfully");
        } catch (Exception e) {
            log.error("Error during provider approval");
            throw e;
        }
    }

    // ---------- SERVICE CONTROLS ----------
//    @Operation(summary = "List categories (paginated)- PUBLIC")
//    @GetMapping("/categories")
//    public Page<ServiceCategory> listCategories(@PageableDefault(size = 20) Pageable pageable) {
//        log.trace("Entering listCategories method");
//        log.debug("Fetching categories with pagination");
//        log.info("List categories API called");
//        Page<ServiceCategory> categories = adminService.listCategories(pageable);
//        log.debug("Categories retrieved successfully");
//        return categories;
//    }

//    @Operation(summary = "Create category")
//    @PostMapping("/categories")
//    public ServiceCategory createCategory(
//            @RequestParam @NotBlank(message = "Category name is required") String name) {
//        log.trace("Entering createCategory method");
//        log.debug("Processing category creation request");
//        log.info("Create category API called");
//        try {
//            ServiceCategory category = adminService.createCategory(name);
//            log.debug("Category created successfully");
//            return category;
//        } catch (Exception e) {
//            log.error("Error during category creation");
//            throw e;
//        }
 //   }

//    @Operation(summary = "Update category")
//    @PutMapping("/categories/{categoryId}")
//    public ServiceCategory updateCategory(
//            @PathVariable Long categoryId,
//            @RequestParam @NotBlank(message = "Category name is required") String name) {
//        log.trace("Entering updateCategory method");
//        log.debug("Processing category update request");
//        log.info("Update category API called");
//        try {
//            ServiceCategory category = adminService.updateCategory(categoryId, name);
//            log.debug("Category updated successfully");
//            return category;
//        } catch (Exception e) {
//            log.error("Error during category update");
//            throw e;
//        }
//    }

//    @Operation(summary = "Delete category")
//    @DeleteMapping("/categories/{categoryId}")
//    public void deleteCategory(@PathVariable Long categoryId) {
//        log.trace("Entering deleteCategory method");
//        log.debug("Processing category deletion request");
//        log.info("Delete category API called");
//        try {
//            adminService.deleteCategory(categoryId);
//            log.debug("Category deleted successfully");
//        } catch (Exception e) {
//            log.error("Error during category deletion");
//            throw e;
//        }
//    }

    @Operation(summary = "Set pricing rule (update subservice price) - ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/subservices/{subServiceId}/price")
    public SubService updatePrice(@PathVariable Long subServiceId, @Valid @RequestBody UpdatePriceRequest req) {
        log.trace("Entering updatePrice method");
        log.debug("Processing subservice price update request");
        log.info("Update subservice price API called");
        try {
            SubService subService = adminService.updateSubServicePrice(subServiceId, req.price());
            log.debug("Subservice price updated successfully");
            return subService;
        } catch (Exception e) {
            log.error("Error during subservice price update");
            throw e;
        }
    }

    // ---------- BOOKING CONTROLS ----------
    @Operation(summary = "View all bookings (paginated)- ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/bookings")
    public org.springframework.data.domain.Page<com.example.ServiceBooking.bookings.Booking> allBookings(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        log.trace("Entering allBookings method");
        log.debug("Fetching all bookings with pagination");
        log.info("View all bookings API called");
        org.springframework.data.domain.Page<com.example.ServiceBooking.bookings.Booking> bookings = adminService.viewAllBookings(pageable);
        log.debug("Bookings retrieved successfully");
        return bookings;
    }

    @Operation(summary = "Manual provider assignment to a booking- ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/bookings/{bookingId}/assign")
    public void assignProvider(@PathVariable Long bookingId, @Valid @RequestBody ManualAssignProviderRequest req) {
        log.trace("Entering assignProvider method");
        log.debug("Processing manual provider assignment");
        log.info("Manual assign provider API called");
        try {
            adminService.manualAssignProvider(bookingId, req.providerId());
            log.debug("Provider assigned successfully");
        } catch (Exception e) {
            log.error("Error during manual provider assignment");
            throw e;
        }
    }

    // ---------- REPORTS ----------
    @Operation(summary = "Booking report between dates (counts)- ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reports/bookings")
    public BookingReportResponse bookingReport(
            @RequestParam @NotNull(message = "From date is required") LocalDateTime from,
            @RequestParam @NotNull(message = "To date is required") LocalDateTime to
    ) {
        log.trace("Entering bookingReport method");
        log.debug("Processing booking report request");
        log.info("Booking report API called");
        try {
            BookingReportResponse response = adminService.bookingReport(from, to);
            log.debug("Booking report generated successfully");
            return response;
        } catch (Exception e) {
            log.error("Error during booking report generation");
            throw e;
        }
    }

    @Operation(summary = "Provider performance report (paginated)- ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reports/providers")
    public org.springframework.data.domain.Page<ProviderPerformanceResponse> providerPerformance(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        log.trace("Entering providerPerformance method");
        log.debug("Processing provider performance report request");
        log.info("Provider performance API called");
        try {
            org.springframework.data.domain.Page<ProviderPerformanceResponse> response = adminService.providerPerformance(pageable);
            log.debug("Provider performance report generated successfully");
            return response;
        } catch (Exception e) {
            log.error("Error during provider performance report generation");
            throw e;
        }
    }
}

