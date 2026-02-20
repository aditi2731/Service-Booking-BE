package com.example.ServiceBooking.support.dto;


import com.example.ServiceBooking.support.dto.TicketStatus;
import jakarta.validation.constraints.Size;

public record UpdateTicketRequest(
        TicketStatus status,               // optional
        @Size(max = 2000) String resolutionNote  // optional
) {}
