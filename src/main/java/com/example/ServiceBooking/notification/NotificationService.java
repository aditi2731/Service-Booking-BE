package com.example.ServiceBooking.notification;

import com.example.ServiceBooking.auth.User;
import com.example.ServiceBooking.auth.UserRepository;
import com.example.ServiceBooking.auth.Role;
import com.example.ServiceBooking.notification.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository repo;
    private final UserRepository userRepo;
    private final NotificationEmailSender emailSender;

    // Used by triggers (Booking/Provider/Payment etc.)

    public void upsertBookingNotification(Long userId, Long bookingId, String message) {
        log.trace("Entering upsertBookingNotification method");
        log.debug("Processing booking notification upsert");
        log.info("Upserting booking notification");

        if (userId == null) {
            log.error("User not found for notification");
            throw new RuntimeException("User not found for notification");
        }
        if (bookingId == null) {
            log.error("Booking not found for notification");
            throw new RuntimeException("Booking not found for notification");
        }
        if (message == null || message.trim().isEmpty()) {
            log.error("Notification message is required");
            throw new RuntimeException("Notification message is required");
        }

        LocalDateTime now = LocalDateTime.now();

        Notification n = repo.findByUserIdAndBookingId(userId, bookingId)
                .orElseGet(() -> {
                    log.debug("Creating new booking notification");
                    Notification x = new Notification();
                    x.setUserId(userId);
                    x.setBookingId(bookingId);
                    x.setCreatedAt(now);
                    x.setUpdatedAt(now);
                    x.setRead(false);
                    x.setSent(false);
                    return x;
                });

        // update same row
        n.setMessage(message.trim());
        n.setUpdatedAt(now);

        // reset delivery/read flags so each new update sends a new email
        n.setSent(false);
        n.setSentAt(null);
        n.setRead(false);
        n.setReadAt(null);

        repo.save(n);
        log.debug("Booking notification upserted successfully");
    }


    public void createSystemNotification(Long userId, String message) {
        log.trace("Entering createSystemNotification method");
        log.debug("Processing system notification creation");
        log.info("Creating system notification");

        if (userId == null) {
            log.error("User not found for notification");
            throw new RuntimeException("User not found for notification");
        }
        if (message == null || message.trim().isEmpty()) {
            log.error("Notification message is required");
            throw new RuntimeException("Notification message is required");
        }

        LocalDateTime now = LocalDateTime.now();

        Notification n = new Notification();
        n.setUserId(userId);
        n.setBookingId(null); // ✅ system notification
        n.setMessage(message.trim());
        n.setRead(false);
        n.setSent(false);
        n.setCreatedAt(now);
        n.setUpdatedAt(now);

        repo.save(n);
        log.debug("System notification created successfully");
    }

//    public void createNotification(Long userId, String message) {
//        if (userId == null) throw new RuntimeException("User not found for notification");
//        if (message == null || message.trim().isEmpty()) throw new RuntimeException("Notification message is required");
//
//        Notification n = new Notification();
//        n.setUserId(userId);
//        n.setMessage(message.trim());
//        n.setRead(false);
//        n.setSent(false);
//        n.setCreatedAt(LocalDateTime.now());
//
//        repo.save(n);
//
//        log.info("Notification created");
//        log.debug("Notification stored ");
//    }

    public Page<NotificationResponse> userNotifications(Long userId, Pageable pageable) {
        log.trace("Entering userNotifications method");
        log.debug("Fetching user notifications");
        log.info("Fetching user notifications ");
        Page<NotificationResponse> notifications = repo.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
        log.debug("User notifications retrieved successfully");
        return notifications;
    }

    public NotificationResponse markAsRead(Long userId, Long notificationId) {
        log.trace("Entering markAsRead method");
        log.debug("Processing mark notification as read");
        log.info("Marking notification as read");

        Notification n = repo.findById(notificationId)
                .orElseThrow(() -> {
                    log.error("Notification not found");
                    return new RuntimeException("Notification not found");
                });

        if (!n.getUserId().equals(userId)) {
            log.error("User not allowed to mark this notification as read");
            throw new RuntimeException("Not allowed");
        }

        if (!n.isRead()) {
            n.setRead(true);
            n.setReadAt(LocalDateTime.now());
            repo.save(n);
            log.debug("Notification marked as read successfully");
        } else {
            log.debug("Notification already marked as read");
        }

        log.info("Notification marked as read");
        return toResponse(n);
    }

    /**
     * Scheduler uses this.
     * Sends pending notifications via Email using SMTP configuration.
     * If sending fails, keep sent=false so it will retry next time.
     */

    public int dispatchPendingNotifications() {
        log.trace("Entering dispatchPendingNotifications method");
        log.debug("Processing pending notifications dispatch");
        log.info("Dispatching pending notifications");

        //  order by updatedAt so the latest status update gets sent in correct sequence
        List<Notification> pending = repo.findTop50BySentFalseOrderByUpdatedAtAsc();

        if (pending.isEmpty()) {
            log.debug("No pending notifications to dispatch");
            return 0;
        }

        log.info("Found pending notifications to dispatch");
        int sentCount = 0;

        for (Notification n : pending) {
            try {
                String email = resolveUserEmail(n.getUserId());

                String subject = "Service Booking Notification";

                String body = """
                          Hello,

                          You have a new notification from Service Booking:

                          %s

                          Regards,
                          Service Booking Team
                          """.formatted(n.getMessage());

                emailSender.send(email, subject, body);

                // mark sent only after successful email send
                n.setSent(true);
                n.setSentAt(LocalDateTime.now());
                sentCount++;
                log.debug("Notification email sent successfully");

            } catch (Exception ex) {
                // keep it pending for retry on next scheduler run
                log.error("Failed to send notification email", ex);
            }
        }

        repo.saveAll(pending);
        log.info("Notification dispatch cycle completed");
        log.debug("Total notifications sent successfully");
        return sentCount;
    }


//    public int dispatchPendingNotifications() {
//        List<Notification> pending = repo.findTop50BySentFalseOrderByCreatedAtAsc();
//        if (pending.isEmpty()) {
//            log.debug("No pending notifications to dispatch");
//            return 0;
//        }
//
//        int sentCount = 0;
//        LocalDateTime now = LocalDateTime.now();
//
//        for (Notification n : pending) {
//            try {
//                String email = resolveUserEmail(n.getUserId());
//
//                String subject = "Service Booking Notification";
//
//                String body = """
//                              Hello,
//
//                              You have a new notification from Service Booking:
//
//                               %s
//
//                              Regards,
//                              Service Booking Team
//                              """.formatted(n.getMessage());
//
//
//                emailSender.send(email, subject, body);
//
//                n.setSent(true);
//                n.setSentAt(now);
//                sentCount++;
//
//            } catch (Exception ex) {
//                // don't throw; keep it pending for retry
//                log.error("Failed to send notification email", ex);
//            }
//        }
//
//        repo.saveAll(pending);
//        log.info("Notification dispatch cycle completed");
//        return sentCount;
//    }

    private String resolveUserEmail(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found");
                    return new RuntimeException("User not found");
                });
        return user.getEmail();
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getNotificationId(),
                n.getMessage(),
                n.isRead(),
                n.isSent(),
                n.getCreatedAt(),
                n.getSentAt(),
                n.getReadAt()
        );
    }


    //Admin related notification
    public void notifyAllAdmins(String message) {
        log.trace("Entering notifyAllAdmins method");
        log.debug("Processing notify all admins");
        log.info("Notifying all admins");

        List<User> admins = userRepo.findByRole(Role.ADMIN);

        if (admins.isEmpty()) {
            log.warn("No admin users found");
        }

        for (User admin : admins) {
            createSystemNotification(admin.getId(), message);
        }

        log.debug("All admins notified successfully");
    }

}
