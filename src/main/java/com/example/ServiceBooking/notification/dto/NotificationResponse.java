package com.example.ServiceBooking.notification.dto;



import java.time.LocalDateTime;

public record NotificationResponse(
        Long notificationId,
        String message,
        boolean read,
        boolean sent,
        LocalDateTime createdAt,
        LocalDateTime sentAt,
        LocalDateTime readAt
) {}

