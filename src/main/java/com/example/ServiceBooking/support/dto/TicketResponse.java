package com.example.ServiceBooking.support.dto;


import com.example.ServiceBooking.support.dto.TicketPriority;
import com.example.ServiceBooking.support.dto.TicketStatus;
import com.example.ServiceBooking.support.dto.TicketType;

import java.time.LocalDateTime;

public record TicketResponse(
        Long ticketId,
        Long bookingId,
        Long customerId,
        Long providerId,
        TicketType ticketType,
        TicketPriority priority,
        TicketStatus status,
        String subject,
        String description,
        String assignedRole,
        Long assignedAgentId,
        String resolutionNote,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime slaDueAt,
        LocalDateTime resolvedAt
) {}

