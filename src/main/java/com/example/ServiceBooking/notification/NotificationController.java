package com.example.ServiceBooking.notification;

import com.example.ServiceBooking.auth.JwtUtil;
import com.example.ServiceBooking.notification.dto.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notification Service", description = "Endpoints for user notifications")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService service;
    private final JwtUtil jwtUtil;

    private Long userId() {
        return jwtUtil.getCurrentUserId();
    }

    @Operation(summary = "Fetch logged-in user notifications")
    @GetMapping

    @PreAuthorize("isAuthenticated()")
    public Page<NotificationResponse> myNotifications(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        log.trace("Entering myNotifications method");
        log.debug("Processing fetch notifications request");
        log.info("Fetch notifications request received");
        try {
            Page<NotificationResponse> notifications = service.userNotifications(userId(), pageable);
            log.debug("Notifications fetched successfully");
            return notifications;
        } catch (Exception e) {
            log.error("Error fetching notifications");
            throw e;
        }
    }

    @Operation(summary = "Mark a notification as read")
    @PutMapping("/{notificationId}/read")
//    @PreAuthorize("isAuthenticated()")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('PROVIDER')")
    public NotificationResponse markRead(@PathVariable Long notificationId) {
        log.trace("Entering markRead method");
        log.debug("Processing mark notification as read request");
        log.info("Mark notification read request received");
        try {
            NotificationResponse response = service.markAsRead(userId(), notificationId);
            log.debug("Notification marked as read successfully");
            return response;
        } catch (Exception e) {
            log.error("Error marking notification as read");
            throw e;
        }
    }

//    // Optional admin endpoint (manual testing)
//    @Operation(summary = "Create notification (ADMIN)")
//    @PostMapping("/create")
//    @PreAuthorize("hasRole('ADMIN')")
//    public void create(@RequestParam Long userId, @RequestParam String message) {
//        log.info("Admin create notification request received");
//        service.createNotification(userId, message);
//    }
}
