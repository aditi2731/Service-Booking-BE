package com.example.ServiceBooking.notification;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatcher {

    private final NotificationService notificationService;

    // every 30 seconds
    @Scheduled(fixedDelay = 30000)
    public void dispatch() {
        int dispatched = notificationService.dispatchPendingNotifications();
        if (dispatched > 0) {
            log.info("Notification dispatcher sent pending notifications");
            log.debug("Dispatcher ran successfully");
        } else {
            log.debug("No pending notifications to dispatch");
        }
    }
}

