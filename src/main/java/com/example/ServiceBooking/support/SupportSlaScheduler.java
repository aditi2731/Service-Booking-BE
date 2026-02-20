package com.example.ServiceBooking.support;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class SupportSlaScheduler {

    private final SupportTicketService ticketService;

    @Scheduled(fixedDelay = 60_000) // every 60s
    public void escalate() {
        log.trace("SLA scheduler triggered");
        try {
            int count = ticketService.escalateBreachedTickets();
            if (count > 0) log.info("Escalated {} tickets due to SLA breach", count);
        } catch (Exception ex) {
            log.error("SLA escalation job failed", ex);
        }
    }
}

