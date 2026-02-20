package com.example.ServiceBooking.support;

import com.example.ServiceBooking.support.dto.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    Page<SupportTicket> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);

    Page<SupportTicket> findByAssignedAgentIdOrderByCreatedAtDesc(Long agentId, Pageable pageable);

    Page<SupportTicket> findByStatusOrderByCreatedAtDesc(TicketStatus status, Pageable pageable);

    List<SupportTicket> findByStatusInAndSlaDueAtBefore(List<TicketStatus> statuses, LocalDateTime now);
}

