package com.example.ServiceBooking.ratings;

import com.example.ServiceBooking.auth.JwtUtil;
import com.example.ServiceBooking.ratings.dto.ProviderRatingSummaryResponse;
import com.example.ServiceBooking.ratings.dto.RatingReviewResponse;
import com.example.ServiceBooking.ratings.dto.SubmitRatingRequest;
import com.example.ServiceBooking.ratings.dto.SubmitReviewRequest;
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

@Tag(name = "Ratings & Reviews Service", description = "Endpoints for submitting and viewing ratings and reviews")
@RestController
@RequestMapping("/ratings")
@RequiredArgsConstructor
@Slf4j
public class RatingReviewController {

    private final RatingReviewService service;
    private final JwtUtil jwtUtil;

    private Long userId() {
        return jwtUtil.getCurrentUserId();
    }

    @Operation(summary = "Submit rating - CUSTOMER")
    @PostMapping("/submit-rating")
    @PreAuthorize("hasRole('CUSTOMER')")
    public RatingReviewResponse submitRating(@Valid @RequestBody SubmitRatingRequest request) {
        log.info("Submit rating request received");
        log.debug("Submit rating payload received");

        RatingReviewResponse response = service.submitRating(userId(), request);

        log.info("Submit rating processed successfully");
        return response;
    }

    @Operation(summary = "Submit review (comment) - CUSTOMER")
    @PostMapping("/submit-review")
    @PreAuthorize("hasRole('CUSTOMER')")
    public RatingReviewResponse submitReview(@Valid @RequestBody SubmitReviewRequest request) {
        log.info("Submit review request received");
        log.debug("Submit review payload received");

        RatingReviewResponse response = service.submitReview(userId(), request);

        log.info("Submit review processed successfully");
        return response;
    }

    @Operation(summary = "Fetch provider ratings (paginated)")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('PROVIDER') or hasRole('CUSTOMER')")
    @GetMapping("/provider/{providerId}")
    public Page<RatingReviewResponse> providerRatings(
            @PathVariable Long providerId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        log.info("Fetch provider ratings request received");
        log.debug("Pagination params received (page/size/sort)");

        Page<RatingReviewResponse> page = service.providerRatings(providerId, pageable);

        log.info("Fetch provider ratings processed successfully");
        return page;
    }

    @Operation(summary = "Fetch provider rating summary (avg + count)- ADMIN/PROVIDER")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROVIDER')")
    @GetMapping("/provider/{providerId}/summary")
    public ProviderRatingSummaryResponse providerSummary(@PathVariable Long providerId) {
        log.info("Fetch provider rating summary request received");
        log.debug("Fetching rating summary ");

        ProviderRatingSummaryResponse response = service.providerRatingSummary(providerId);

        log.info("Fetch provider rating summary processed successfully");
        return response;
    }

    @Operation(summary = "Fetch service reviews (paginated)")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('PROVIDER') or hasRole('CUSTOMER')")
    @GetMapping("/service/{serviceId}")
    public Page<RatingReviewResponse> serviceReviews(
            @PathVariable Long serviceId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        log.info("Fetch service reviews request received");
        log.debug("Pagination params received (page/size/sort).");

        Page<RatingReviewResponse> page = service.serviceReviews(serviceId, pageable);

        log.info("Fetch service reviews processed successfully");
        return page;
    }
}
