package com.example.ServiceBooking.servicecatalog;

import com.example.ServiceBooking.servicecatalog.dto.SubServiceRequest;
import com.example.ServiceBooking.servicecatalog.dto.SubServiceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "SubServiceCategory", description = "Endpoints for managing sub-services within service categories")
@RestController
@RequestMapping("/catalog/services")
@RequiredArgsConstructor
public class SubServiceController {

    private final SubServiceService service;

    @Operation(summary = "Add a new sub-service to a category - ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    // Add new sub-service to a category
    @PostMapping("/category/{categoryId}")
    public void add(
            @PathVariable Long categoryId,
            @Valid @RequestBody SubServiceRequest request
    ) {
        log.trace("Entering add method");
        log.debug("Processing add sub-service request");
        log.info("Add sub-service requested");
        try {
            service.add(categoryId, request);
            log.debug("Sub-service added successfully");
        } catch (Exception e) {
            log.error("Error adding sub-service");
            throw e;
        }
    }

    @Operation(summary = "Update an existing sub-service - ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    // Update existing sub-service
    @PutMapping("/{id}")
    public void update(
            @PathVariable Long id,
            @Valid @RequestBody SubServiceRequest request
    ) {
        log.trace("Entering update method");
        log.debug("Processing update sub-service request");
        log.info("Update sub-service requested");
        try {
            service.update(id, request);
            log.debug("Sub-service updated successfully");
        } catch (Exception e) {
            log.error("Error updating sub-service");
            throw e;
        }
    }

    @Operation(summary = "Delete a subservice by ID - ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    // Delete sub-service by ID
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.trace("Entering delete method");
        log.debug("Processing delete sub-service request");
        log.info("Delete sub-service requested");
        try {
            service.delete(id);
            log.debug("Sub-service deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting sub-service");
            throw e;
        }
    }

    @Operation(summary = "Get all sub-service details by CategoryID")
    // Get all sub-services in a category
    @GetMapping("/category/{categoryId}")
    public List<SubServiceResponse> byCategory(@PathVariable Long categoryId) {
        log.trace("Entering byCategory method");
        log.debug("Processing get sub-services by category request");
        log.info("Get sub-services by category requested");
        try {
            List<SubServiceResponse> subServices = service.byCategory(categoryId);
            log.debug("Sub-services retrieved successfully");
            return subServices;
        } catch (Exception e) {
            log.error("Error retrieving sub-services by category");
            throw e;
        }
    }

    @Operation(summary = "Search sub-services by name or description")
    // Search sub-services by name or description
    @GetMapping("/search")
    public List<SubServiceResponse> search(
            @RequestParam
            @NotBlank(message = "Search query is required")
            String q) {
        log.trace("Entering search method");
        log.debug("Processing search sub-services request");
        log.info("Search sub-services requested");
        try {
            List<SubServiceResponse> results = service.search(q);
            log.debug("Sub-service search completed successfully");
            return results;
        } catch (Exception e) {
            log.error("Error searching sub-services");
            throw e;
        }
    }
}

