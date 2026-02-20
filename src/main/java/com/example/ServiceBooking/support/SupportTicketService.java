package com.example.ServiceBooking.support;

import com.example.ServiceBooking.auth.Role;
import com.example.ServiceBooking.auth.Status;
import com.example.ServiceBooking.auth.User;
import com.example.ServiceBooking.auth.UserRepository;
import com.example.ServiceBooking.bookings.BookingRepository;
import com.example.ServiceBooking.notification.NotificationService;
import com.example.ServiceBooking.support.dto.*;
import jakarta.transaction.Transactional;
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
public class SupportTicketService {

    private final SupportTicketRepository ticketRepo;
    private final SupportRoutingService routingService;
    private final UserRepository userRepo;
    private final BookingRepository bookingRepo;
    private final NotificationService notificationService;

    @Transactional
    public TicketResponse createTicket(Long customerId, CreateTicketRequest req) {
        log.trace("Entering createTicket()");
        log.debug("Validating ticket creation request (IDs not logged)");
        log.info("Customer creating support ticket");

        // Optional booking validation
        if (req.bookingId() != null) {
            bookingRepo.findById(req.bookingId())
                    .orElseThrow(() -> {
                        log.warn("Ticket create failed: booking not found");
                        return new RuntimeException("Booking not found");
                    });
        }

        TicketPriority priority = (req.priority() == null) ? TicketPriority.MEDIUM : req.priority();

        Role assignedRole = routingService.route(req.ticketType(), priority);

        Long agentId = pickAgentId(assignedRole);

        SupportTicket t = new SupportTicket();
        t.setCustomerId(customerId);
        t.setBookingId(req.bookingId());
        t.setProviderId(req.providerId());
        t.setTicketType(req.ticketType());
        t.setPriority(priority);
        t.setStatus(TicketStatus.OPEN);
        t.setSubject(req.subject().trim());
        t.setDescription(req.description().trim());
        t.setAssignedRole(assignedRole.name());
        t.setAssignedAgentId(agentId);

        LocalDateTime now = LocalDateTime.now();
        t.setCreatedAt(now);
        t.setUpdatedAt(now);
        t.setSlaDueAt(now.plusHours(slaHours(priority))); // SLA

        SupportTicket saved = ticketRepo.save(t);

        // Notify assigned agent + customer
        if (agentId != null) {
            notificationService.createSystemNotification(
                    agentId,
                    "New Ticket Assigned: #" + saved.getTicketId() + " (" + saved.getTicketType() + ")"
            );
        }

        notificationService.createSystemNotification(
                customerId,
                "Ticket created successfully. Ticket #" + saved.getTicketId()
        );

        log.info("Ticket created successfully");
        return toResponse(saved);
    }

    public Page<TicketResponse> myTickets(Long customerId, Pageable pageable) {
        log.trace("Entering myTickets()");
        log.debug("Fetching tickets for customer");
        log.info("Customer ticket history requested");
        return ticketRepo.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable).map(this::toResponse);
    }

    public Page<TicketResponse> assignedTickets(Long agentId, Pageable pageable) {
        log.trace("Entering assignedTickets()");
        log.debug("Fetching tickets assigned to agent");
        log.info("Agent assigned ticket list requested");
        return ticketRepo.findByAssignedAgentIdOrderByCreatedAtDesc(agentId, pageable).map(this::toResponse);
    }

    @Transactional
    public TicketResponse updateTicketAsAgent(Long agentId, Role agentRole, Long ticketId, UpdateTicketRequest req) {
        log.trace("Entering updateTicketAsAgent()");
        log.debug("Validating agent update request");
        log.info("Support agent updating ticket");

        SupportTicket t = ticketRepo.findById(ticketId)
                .orElseThrow(() -> {
                    log.warn("Ticket not found");
                    return new RuntimeException("Ticket not found");
                });

        // Agent must be assigned OR manager can update any
        boolean isManager = agentRole == Role.SUPPORT_MANAGER;
        if (!isManager) {
            if (t.getAssignedAgentId() == null || !t.getAssignedAgentId().equals(agentId)) {
                log.warn("Agent update blocked: not assigned");
                throw new RuntimeException("Not allowed: ticket not assigned to you");
            }
        }

        if (req.status() != null) {
            t.setStatus(req.status());
            if (req.status() == TicketStatus.RESOLVED || req.status() == TicketStatus.CLOSED) {
                t.setResolvedAt(LocalDateTime.now());
            }
        }

        if (req.resolutionNote() != null && !req.resolutionNote().isBlank()) {
            t.setResolutionNote(req.resolutionNote().trim());
        }

        t.setUpdatedAt(LocalDateTime.now());

        SupportTicket saved = ticketRepo.save(t);

        // Notify customer on updates
        notificationService.createSystemNotification(
                saved.getCustomerId(),
                "Ticket #" + saved.getTicketId() + " updated. Status: " + saved.getStatus()
        );

        log.info("Ticket updated successfully");
        return toResponse(saved);
    }

    // SLA Escalation Job (called by scheduler)
    @Transactional
    public int escalateBreachedTickets() {
        log.trace("Entering escalateBreachedTickets()");
        log.debug("Checking SLA breaches");
        log.info("Running SLA escalation scan");

        List<SupportTicket> breached = ticketRepo.findByStatusInAndSlaDueAtBefore(
                List.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS),
                LocalDateTime.now()
        );

        if (breached.isEmpty()) {
            log.debug("No SLA breaches found");
            return 0;
        }

        for (SupportTicket t : breached) {
            t.setPriority(TicketPriority.HIGH);
            t.setAssignedRole(Role.SUPPORT_MANAGER.name());
            t.setAssignedAgentId(pickAgentId(Role.SUPPORT_MANAGER));
            t.setUpdatedAt(LocalDateTime.now());

            // notify customer + assigned manager
            notificationService.createSystemNotification(
                    t.getCustomerId(),
                    "Ticket #" + t.getTicketId() + " escalated due to SLA breach"
            );

            if (t.getAssignedAgentId() != null) {
                notificationService.createSystemNotification(
                        t.getAssignedAgentId(),
                        "Escalated Ticket Assigned: #" + t.getTicketId()
                );
            }
        }

        ticketRepo.saveAll(breached);

        log.info("SLA escalation applied to {} tickets", breached.size());
        return breached.size();
    }

    // -------- helpers --------

    private int slaHours(TicketPriority p) {
        // simple SLA rules (can evolve later)
        return switch (p) {
            case HIGH -> 2;
            case MEDIUM -> 6;
            case LOW -> 24;
        };
    }

    private Long pickAgentId(Role role) {
        log.debug("Picking available agent for role {}", role);

        // simple routing: first ACTIVE user with that role
        // (you can later add agent availability/online)
        User agent = userRepo.findFirstByRoleAndStatus(role, Status.ACTIVE).orElse(null);
        return agent == null ? null : agent.getId();
    }

    private TicketResponse toResponse(SupportTicket t) {
        return new TicketResponse(
                t.getTicketId(),
                t.getBookingId(),
                t.getCustomerId(),
                t.getProviderId(),
                t.getTicketType(),
                t.getPriority(),
                t.getStatus(),
                t.getSubject(),
                t.getDescription(),
                t.getAssignedRole(),
                t.getAssignedAgentId(),
                t.getResolutionNote(),
                t.getCreatedAt(),
                t.getUpdatedAt(),
                t.getSlaDueAt(),
                t.getResolvedAt()
        );
    }
}

