package com.example.ServiceBooking.providermanagement;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
        name = "provider_availability",
        indexes = {
                @Index(name = "idx_avl_provider_date", columnList = "provider_id,slot_date"),
                @Index(name = "idx_avl_date_status", columnList = "slot_date,status")
        }
)
@Getter
@Setter
public class ProviderAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="provider_id", nullable = false)
    private Long providerId;

    @Column(name="slot_date", nullable = false)
    private LocalDate date;

    @Column(name="start_time", nullable = false)
    private LocalTime startTime;

    @Column(name="end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvailabilityStatus status;
}

