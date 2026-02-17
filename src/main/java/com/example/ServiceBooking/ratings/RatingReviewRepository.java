package com.example.ServiceBooking.ratings;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RatingReviewRepository extends JpaRepository<RatingReview, Long> {

    boolean existsByBookingId(Long bookingId);

    Optional<RatingReview> findByBookingId(Long bookingId);

    Page<RatingReview> findByProviderId(Long providerId, Pageable pageable);

    Page<RatingReview> findByServiceId(Long serviceId, Pageable pageable);

    @Query("select avg(r.stars) from RatingReview r where r.providerId = :providerId")
    Double avgStarsForProvider(Long providerId);

    @Query("select count(r) from RatingReview r where r.providerId = :providerId")
    Long countForProvider(Long providerId);
}

