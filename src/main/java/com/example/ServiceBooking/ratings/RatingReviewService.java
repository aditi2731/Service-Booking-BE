package com.example.ServiceBooking.ratings;

import com.example.ServiceBooking.bookings.Booking;
import com.example.ServiceBooking.bookings.BookingRepository;
import com.example.ServiceBooking.bookings.dto.BookingStatus;
import com.example.ServiceBooking.ratings.dto.ProviderRatingSummaryResponse;
import com.example.ServiceBooking.ratings.dto.RatingReviewResponse;
import com.example.ServiceBooking.ratings.dto.SubmitRatingRequest;
import com.example.ServiceBooking.ratings.dto.SubmitReviewRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingReviewService {

    private final RatingReviewRepository repo;
    private final BookingRepository bookingRepo;

    @Transactional
    public RatingReviewResponse submitRating(Long customerId, SubmitRatingRequest request) {
        log.info("Processing submit rating request");
        log.debug("Validating rating request");

        try {
            validateStars(request.stars());

            Booking booking = bookingRepo.findById(request.bookingId())
                    .orElseThrow(() -> {
                        log.warn("Submit rating failed: booking not found");
                        return new RuntimeException("Booking not found");
                    });

            ensureCustomerOwnsBooking(customerId, booking);
            ensureBookingCompleted(booking);
            ensureProviderAssigned(booking);

            RatingReview rr = repo.findByBookingId(request.bookingId()).orElse(null);

            if (rr == null) {
                log.debug("No existing rating/review record found; creating new record");
                rr = new RatingReview();
                rr.setBookingId(booking.getId());
                rr.setProviderId(booking.getProviderId());
                rr.setServiceId(booking.getServiceId());
                rr.setCreatedAt(LocalDateTime.now());
            } else {
                log.debug("Existing rating/review record found; updating stars only");
            }

            rr.setStars(request.stars());

            RatingReview saved = repo.save(rr);
            log.info("Submit rating processed successfully");
            return toResponse(saved);

        } catch (RuntimeException ex) {
            log.warn("Submit rating failed due to business validation: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while submitting rating", ex);
            throw ex;
        }
    }

    @Transactional
    public RatingReviewResponse submitReview(Long customerId, SubmitReviewRequest request) {
        log.info("Processing submit review request");
        log.debug("Validating review request");

        try {
            if (request.comment() == null || request.comment().trim().isEmpty()) {
                log.warn("Submit review failed: comment missing/blank");
                throw new RuntimeException("Comment is required");
            }

            Booking booking = bookingRepo.findById(request.bookingId())
                    .orElseThrow(() -> {
                        log.warn("Submit review failed: booking not found");
                        return new RuntimeException("Booking not found");
                    });

            ensureCustomerOwnsBooking(customerId, booking);
            ensureBookingCompleted(booking);
            ensureProviderAssigned(booking);

            RatingReview rr = repo.findByBookingId(request.bookingId()).orElse(null);

            if (rr == null) {
                log.debug("No existing rating/review record found; creating new record");
                rr = new RatingReview();
                rr.setBookingId(booking.getId());
                rr.setProviderId(booking.getProviderId());
                rr.setServiceId(booking.getServiceId());
                rr.setCreatedAt(LocalDateTime.now());
            } else {
                log.debug("Existing rating/review record found; updating comment only");
            }

            rr.setComment(request.comment().trim());

            RatingReview saved = repo.save(rr);
            log.info("Submit review processed successfully");
            return toResponse(saved);

        } catch (RuntimeException ex) {
            log.warn("Submit review failed due to business validation: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while submitting review", ex);
            throw ex;
        }
    }

    public Page<RatingReviewResponse> providerRatings(Long providerId, Pageable pageable) {
        log.info("Fetching provider ratings ");
        log.debug("Pagination params received ");

        try {
            Page<RatingReviewResponse> page = repo.findByProviderId(providerId, pageable).map(this::toResponse);
            log.info("Provider ratings fetched successfully");
            return page;
        } catch (RuntimeException ex) {
            log.warn("Provider ratings fetch failed: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while fetching provider ratings", ex);
            throw ex;
        }
    }

    public Page<RatingReviewResponse> serviceReviews(Long serviceId, Pageable pageable) {
        log.info("Fetching service reviews ");
        log.debug("Pagination params received ");

        try {
            Page<RatingReviewResponse> page = repo.findByServiceId(serviceId, pageable).map(this::toResponse);
            log.info("Service reviews fetched successfully");
            return page;
        } catch (RuntimeException ex) {
            log.warn("Service reviews fetch failed: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while fetching service reviews", ex);
            throw ex;
        }
    }

    public ProviderRatingSummaryResponse providerRatingSummary(Long providerId) {
        log.info("Fetching provider rating summary");
        log.debug("Computing provider rating summary ");

        try {
            Double avg = repo.avgStarsForProvider(providerId);
            Long count = repo.countForProvider(providerId);

            ProviderRatingSummaryResponse response = new ProviderRatingSummaryResponse(
                    providerId,
                    avg == null ? 0.0 : avg,
                    count == null ? 0L : count
            );

            log.info("Provider rating summary computed successfully");
            return response;

        } catch (RuntimeException ex) {
            log.warn("Provider rating summary computation failed: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while computing provider rating summary", ex);
            throw ex;
        }
    }

    // ---- helpers ----

    private void validateStars(Integer stars) {
        if (stars == null) {
            log.warn("Stars missing in rating request");
            throw new RuntimeException("Stars is required");
        }
        if (stars < 1 || stars > 5) {
            log.warn("Stars out of range in rating request");
            throw new RuntimeException("Stars must be between 1 and 5");
        }
    }

    private void ensureCustomerOwnsBooking(Long customerId, Booking booking) {
        if (!booking.getCustomerId().equals(customerId)) {
            log.warn("Rating/review blocked: booking ownership mismatch");
            throw new RuntimeException("Not allowed: booking does not belong to you");
        }
    }

    private void ensureBookingCompleted(Booking booking) {
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            log.warn("Rating/review blocked: booking not completed");
            throw new RuntimeException("Rating/review allowed only after booking is COMPLETED");
        }
    }

    private void ensureProviderAssigned(Booking booking) {
        if (booking.getProviderId() == null) {
            log.warn("Rating/review blocked: provider not assigned");
            throw new RuntimeException("Provider not assigned to this booking");
        }
    }

    private RatingReviewResponse toResponse(RatingReview rr) {
        return new RatingReviewResponse(
                rr.getRatingId(),
                rr.getBookingId(),
                rr.getProviderId(),
                rr.getServiceId(),
                rr.getStars(),
                rr.getComment(),
                rr.getCreatedAt()
        );
    }
}
