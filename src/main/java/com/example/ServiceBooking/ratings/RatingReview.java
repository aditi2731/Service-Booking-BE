package com.example.ServiceBooking.ratings;



import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ratings_reviews",
        uniqueConstraints = {
                // ensures only 1 rating/review per booking
                @UniqueConstraint(name = "uk_rating_booking", columnNames = {"booking_id"})
        },
        indexes = {
                @Index(name = "idx_rr_provider", columnList = "provider_id"),
                @Index(name = "idx_rr_service", columnList = "service_id")
        }
)
@Getter
@Setter
public class RatingReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ratingId;

    @Column(name = "booking_id", nullable = false, unique = true)
    private Long bookingId;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    // store serviceId too so we can fetch service reviews without joining booking table
    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    // 1..5 (nullable: review can be submitted without rating first)
    private Integer stars;

    // review text
    @Column(length = 1000)
    private String comment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}

