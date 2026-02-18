package com.example.ServiceBooking.bookings;

import com.example.ServiceBooking.bookings.dto.BookingStatus;
import com.example.ServiceBooking.servicecatalog.SubService;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // CUSTOMER (from users table)
    private Long customerId;
    private String city;

    // PROVIDER (assigned later)
    private Long providerId;

    // SubService ID
    private Long serviceId;

    private LocalDateTime dateTime;
    private String location;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private LocalDateTime createdAt;

    // Added from the SubService entity to store the price at the time of booking
//    private BigDecimal basePrice;
    private BigDecimal price;
    /**
     * Store only hash (BCrypt). Never store raw OTP.
     */
    @Column(length = 100)
    private String startOtpHash;

    private LocalDateTime startOtpGeneratedAt;

    /**
     * When provider successfully verifies OTP.
     */
    private LocalDateTime startOtpVerifiedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = BookingStatus.PENDING;
    }
}

