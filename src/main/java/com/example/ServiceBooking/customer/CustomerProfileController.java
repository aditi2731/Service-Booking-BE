package com.example.ServiceBooking.customer;

import com.example.ServiceBooking.auth.JwtUtil;
import com.example.ServiceBooking.customer.dto.AddressRequest;
import com.example.ServiceBooking.customer.dto.CustomerProfileResponse;
import com.example.ServiceBooking.customer.dto.UpdateCustomerProfileRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Tag(name = "Customer Profile", description = "Customer Profile & Address Service")
@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerProfileController {

    private final CustomerProfileService service;

    private Long userId() {
        return JwtUtil.getCurrentUserId();
    }

    @Operation(summary = "Get current customer profile-CUSTOMER")
    // Get current customer profile
    @GetMapping("/profile")
    public CustomerProfileResponse getProfile() {
        log.trace("Entering getProfile method");
        log.debug("Processing get customer profile request");
        log.info("Get customer profile requested");
        try {
            CustomerProfileResponse response = service.getProfile(userId());
            log.debug("Customer profile retrieved successfully");
            return response;
        } catch (Exception e) {
            log.error("Error retrieving customer profile");
            throw e;
        }
    }

    @Operation(summary = "Update customer profile.-CUSTOMER")
    // Update customer profile (only name for now)
    @PutMapping("/profile")
    public void updateProfile(@Valid @RequestBody UpdateCustomerProfileRequest request) {
        log.trace("Entering updateProfile method");
        log.debug("Processing update customer profile request");
        log.info("Update customer profile requested");
        try {
            service.updateProfile(userId(), request.name());
            log.debug("Customer profile updated successfully");
        } catch (Exception e) {
            log.error("Error updating customer profile");
            throw e;
        }
    }

    @Operation(summary = "Add new address for customer.- CUSTOMER")
    // Add new address
    @PostMapping("/address")
    public void addAddress(@Valid @RequestBody AddressRequest request) {
        log.trace("Entering addAddress method");
        log.debug("Processing add address request");
        log.info("Add address requested");
        try {
            service.addAddress(userId(), request);
            log.debug("Address added successfully");
        } catch (Exception e) {
            log.error("Error adding address");
            throw e;
        }
    }

    @Operation(summary = "Update existing address for customer.- CUSTOMER")
    // Update existing address
    @PutMapping("/address/{id}")
    public void updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request
    ) {
        log.trace("Entering updateAddress method");
        log.debug("Processing update address request");
        log.info("Update address requested");
        try {
            service.updateAddress(id, request, userId());
            log.debug("Address updated successfully");
        } catch (Exception e) {
            log.error("Error updating address");
            throw e;
        }
    }

    @Operation(summary = "Delete address for customer.- CUSTOMER")
    // Delete address
    @DeleteMapping("/address/{id}")
    public void deleteAddress(@PathVariable Long id) {
        log.trace("Entering deleteAddress method");
        log.debug("Processing delete address request");
        log.info("Delete address requested");
        try {
            service.deleteAddress(id, userId());
            log.debug("Address deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting address");
            throw e;
        }
    }
}

