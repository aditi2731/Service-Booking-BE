package com.example.ServiceBooking.support;

import com.example.ServiceBooking.support.dto.TicketPriority;
import com.example.ServiceBooking.support.dto.TicketStatus;
import com.example.ServiceBooking.support.dto.TicketType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "support_tickets",
        indexes = {
                @Index(name = "idx_ticket_customer", columnList = "customer_id"),
                @Index(name = "idx_ticket_assigned_agent", columnList = "assigned_agent_id"),
                @Index(name = "idx_ticket_status", columnList = "status"),
                @Index(name = "idx_ticket_type", columnList = "ticket_type")
        })
@Getter
@Setter
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;

    @Column(name = "booking_id")
    private Long bookingId; // optional (some technical issues might not be booking-specific)

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "provider_id")
    private Long providerId; // optional

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_type", nullable = false)
    private TicketType ticketType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Column(name = "subject", nullable = false, length = 120)
    private String subject;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Column(name = "assigned_role")
    private String assignedRole; // SUPPORT_L1 / SUPPORT_REFUND / SUPPORT_PROVIDER / SUPPORT_MANAGER

    @Column(name = "assigned_agent_id")
    private Long assignedAgentId;

    @Column(name = "resolution_note", length = 2000)
    private String resolutionNote;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "sla_due_at")
    private LocalDateTime slaDueAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}

