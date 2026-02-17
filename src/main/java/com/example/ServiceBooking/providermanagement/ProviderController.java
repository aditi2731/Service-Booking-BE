package com.example.ServiceBooking.providermanagement;



import com.example.ServiceBooking.bookings.Booking;
import com.example.ServiceBooking.bookings.dto.BookingStatus;
import com.example.ServiceBooking.providermanagement.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "Provider Management", description = "Provider dashboard APIs")
@RestController
@RequestMapping("/api/provider")
@RequiredArgsConstructor

public class ProviderController {

    private final PService service;
    private final ProviderProfileRepository repo;

    private Long userId() {
        return Long.valueOf(
                org.springframework.security.core.context.SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal().toString()
        );
    }

    @Operation(summary = "Initial setup of provider profile- PROVIDER")
    @PreAuthorize("hasRole('PROVIDER')")
    // Initial setup of provider profile
    @PostMapping("/setup")
    public void setup(@RequestBody ProviderSetupRequest request) {
        log.trace("Entering setup method");
        log.debug("Processing provider setup request");
        log.info("Provider setup requested");
        try {
            service.setupProvider(userId(), request);
            log.debug("Provider setup completed successfully");
        } catch (Exception e) {
            log.error("Error during provider setup");
            throw e;
        }
    }

    @Operation(summary = "Upload required documents for provider profile- PROVIDER")
    @PreAuthorize("hasRole('PROVIDER')")
    // Upload required documents
    @PostMapping("/documents")
    public void uploadDoc(@RequestBody DocumentRequest request) {
        log.trace("Entering uploadDoc method");
        log.debug("Processing document upload request");
        log.info("Document upload requested");
        try {
            service.uploadDocument(userId(), request);
            log.debug("Document uploaded successfully");
        } catch (Exception e) {
            log.error("Error uploading document");
            throw e;
        }
    }

    @Operation(summary = "Toggle availability status- PROVIDER")
    @PreAuthorize("hasRole('PROVIDER')")
    // Toggle availability status (online/offline)
    @PutMapping("/availability")
    public void toggle(@RequestBody AvailabilityRequest request) {
        log.trace("Entering toggle method");
        log.debug("Processing availability toggle request");
        log.info("Toggle availability requested");
        try {
            service.toggleAvailability(userId(), request.isOnline());
            log.debug("Availability toggled successfully");
        } catch (Exception e) {
            log.error("Error toggling availability");
            throw e;
        }
    }

    @Operation(summary = "Get current month earnings for provider- PROVIDER")
    @PreAuthorize("hasRole('PROVIDER')")
    // Get current month's earnings
    @GetMapping("/earnings")
    public EarningsResponse earnings() {
        log.trace("Entering earnings method");
        log.debug("Processing earnings request");
        log.info("Earnings request received");
        try {
            EarningsResponse response = service.getEarnings(userId());
            log.debug("Earnings retrieved successfully");
            return response;
        } catch (Exception e) {
            log.error("Error retrieving earnings");
            throw e;
        }
    }

    @Operation(summary = "View available jobs for provider- PROVIDER")
    @PreAuthorize("hasRole('PROVIDER')")
    // View available jobs for provider
    @GetMapping("/jobs")
    public List<Booking> viewJobs() {
        log.trace("Entering viewJobs method");
        log.debug("Processing view jobs request");
        log.info("View jobs request received");
        try {
            List<Booking> jobs = service.viewAvailableJobs(userId());
            log.debug("Jobs retrieved successfully");
            return jobs;
        } catch (Exception e) {
            log.error("Error retrieving jobs");
            throw e;
        }
    }

    @Operation(summary = "Accept a job-PROVIDER")
    @PreAuthorize("hasRole('PROVIDER')")
    // Accept a job
    @PutMapping("/jobs/{id}/accept")
    public void accept(@PathVariable Long id) {
        log.trace("Entering accept method");
        log.debug("Processing job acceptance request");
        log.info("Job acceptance requested");
        try {
            service.acceptJob(userId(), id);
            log.debug("Job accepted successfully");
        } catch (Exception e) {
            log.error("Error accepting job");
            throw e;
        }
    }

    @Operation(summary = "Reject a job-PROVIDER")
    @PreAuthorize("hasRole('PROVIDER')")
    // Reject a job
    @PutMapping("/jobs/{id}/reject")
    public void reject(@PathVariable Long id) {
        log.trace("Entering reject method");
        log.debug("Processing job rejection request");
        log.info("Job rejection requested");
        try {
            service.rejectJob(userId(), id);
            log.debug("Job rejection processed successfully");
        } catch (Exception e) {
            log.error("Error rejecting job");
            throw e;
        }
    }

    @Operation(summary = "Update job status- PROVIDER")
    @PreAuthorize("hasRole('PROVIDER')")
   // Update job status
    @PutMapping("/jobs/{id}/status")
    public void updateStatus(@PathVariable Long id,
                             @RequestParam BookingStatus status) {
        log.trace("Entering updateStatus method");
        log.debug("Processing job status update request");
        log.info("Job status update requested");
        try {
            service.updateJobStatus(userId(), id, status);
            log.debug("Job status updated successfully");
        } catch (Exception e) {
            log.error("Error updating job status");
            throw e;
        }
    }


    @Operation(summary = "Approve provider profile- ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    // Approve provider profile after review
    @PutMapping("/{id}/approve")
    public void approve(@PathVariable Long id) {
        log.trace("Entering approve method");
        log.debug("Processing provider approval request");
        log.info("Provider approval requested");
        try {
            ProviderProfile profile = repo.findById(id)
                    .orElseThrow(() -> {
                        log.error("Provider not found");
                        return new RuntimeException("Provider not found");
                    });
            profile.setApproved(true);
            repo.save(profile);
            log.debug("Provider approved successfully");
        } catch (Exception e) {
            log.error("Error approving provider");
            throw e;
        }
    }

    @Operation(summary = "Reject provider profile with reason- ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    // Reject provider profile with reason
    @PutMapping("/{id}/reject")
    public void rejectProvider(@PathVariable Long id, @RequestBody String reason) {
        log.trace("Entering rejectProvider method");
        log.debug("Processing provider rejection request");
        log.info("Provider rejection requested");
        try {
            ProviderProfile profile = repo.findById(id)
                    .orElseThrow(() -> {
                        log.error("Provider not found");
                        return new RuntimeException("Provider not found");
                    });
            profile.setApproved(false);
            profile.setRejectionReason(reason);
            repo.save(profile);
            log.debug("Provider rejected successfully");
        } catch (Exception e) {
            log.error("Error rejecting provider");
            throw e;
        }
    }

    // Slot booking
    @Operation(summary = "Set availability window (creates hourly slots) - PROVIDER")
    @PutMapping("/availability/window")
    @PreAuthorize("hasRole('PROVIDER')")
    public void setAvailabilityWindow(@RequestBody @jakarta.validation.Valid AvailabilityWindowRequest req) {
        service.setAvailabilityWindow(userId(), req.date(), req.startTime(), req.endTime());
    }



}

