package com.example.ServiceBooking.notification;



import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications",
        indexes = {
                @Index(name = "idx_notif_user", columnList = "user_id"),
                @Index(name = "idx_notif_read", columnList = "is_read"),
                @Index(name = "idx_notif_sent", columnList = "is_sent")
        })
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(nullable = false, length = 400)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    // for scheduler (mock delivery)
    @Column(name = "is_sent", nullable = false)
    private boolean sent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;
}

