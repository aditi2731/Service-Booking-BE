package com.example.ServiceBooking.support;


import com.example.ServiceBooking.auth.Role;
import com.example.ServiceBooking.support.dto.TicketPriority;
import com.example.ServiceBooking.support.dto.TicketType;
import org.springframework.stereotype.Service;

@Service
public class SupportRoutingService {

    public Role route(TicketType type, TicketPriority priority) {
        // HIGH always goes to manager
        if (priority == TicketPriority.HIGH) return Role.SUPPORT_MANAGER;

        return switch (type) {
            case REFUND_REQUEST -> Role.SUPPORT_REFUND;
            case PROVIDER_PROBLEM -> Role.SUPPORT_PROVIDER;
            case SERVICE_ISSUE, BOOKING_PROBLEM, TECHNICAL_ISSUE -> Role.SUPPORT_L1;
        };
    }
}

