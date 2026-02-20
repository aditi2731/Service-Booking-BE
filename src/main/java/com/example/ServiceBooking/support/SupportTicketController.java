package com.example.ServiceBooking.support;


import com.example.ServiceBooking.auth.JwtUtil;
import com.example.ServiceBooking.auth.Role;
import com.example.ServiceBooking.support.dto.CreateTicketRequest;
import com.example.ServiceBooking.support.dto.TicketResponse;
import com.example.ServiceBooking.support.dto.UpdateTicketRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Support / Ticket Service", description = "Customer support tickets with routing, SLA escalation and agent handling")
@RestController
@RequestMapping("/support")
@RequiredArgsConstructor
@Slf4j
public class SupportTicketController {

    private final SupportTicketService ticketService;
    private final JwtUtil jwtUtil;

    private Long userId() {
        return jwtUtil.getCurrentUserId();
    }

    private Role role() {
        return jwtUtil.getCurrentUserRole(); // if you don’t have this, see note below
    }

    // CUSTOMER creates ticket
    @Operation(summary = "Create a support ticket - CUSTOMER")
    @PostMapping("/tickets")
    @PreAuthorize("hasRole('CUSTOMER')")
    public TicketResponse create(@Valid @RequestBody CreateTicketRequest req) {
        log.info("Support ticket create request received");
        log.debug("Creating ticket");
        return ticketService.createTicket(userId(), req);
    }

    // CUSTOMER sees own tickets
    @Operation(summary = "My ticket history - CUSTOMER")
    @GetMapping("/tickets/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Page<TicketResponse> myTickets(@PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        log.info("Customer ticket history request received");
        return ticketService.myTickets(userId(), pageable);
    }

    // SUPPORT agent sees assigned tickets
    @Operation(summary = "Assigned tickets - SUPPORT roles")
    @GetMapping("/tickets/assigned")
    @PreAuthorize("hasAnyRole('SUPPORT_L1','SUPPORT_REFUND','SUPPORT_PROVIDER','SUPPORT_MANAGER')")
    public Page<TicketResponse> assigned(@PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        log.info("Agent assigned tickets request received");
        return ticketService.assignedTickets(userId(), pageable);
    }

    // SUPPORT agent updates ticket
    @Operation(summary = "Update ticket (status/resolution) - SUPPORT roles")
    @PutMapping("/tickets/{ticketId}")
    @PreAuthorize("hasAnyRole('SUPPORT_L1','SUPPORT_REFUND','SUPPORT_PROVIDER','SUPPORT_MANAGER')")
    public TicketResponse update(@PathVariable Long ticketId, @Valid @RequestBody UpdateTicketRequest req) {
        log.info("Ticket update request received");
        log.debug("Updating ticket");
        return ticketService.updateTicketAsAgent(userId(), role(), ticketId, req);
    }
}

