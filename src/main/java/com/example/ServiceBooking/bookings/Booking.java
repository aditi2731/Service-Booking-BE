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
}

