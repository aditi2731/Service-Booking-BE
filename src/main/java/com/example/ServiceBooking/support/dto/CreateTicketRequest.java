package com.example.ServiceBooking.support.dto;



import com.example.ServiceBooking.support.dto.TicketPriority;
import com.example.ServiceBooking.support.dto.TicketType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
        Long bookingId,                // optional
        Long providerId,               // optional
        @NotNull TicketType ticketType,
        TicketPriority priority,       // optional default MEDIUM
        @NotBlank @Size(max = 120) String subject,
        @NotBlank @Size(max = 2000) String description
) {}

