package com.example.ServiceBooking.notification;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Notification> findTop50BySentFalseOrderByCreatedAtAsc();
    List<Notification> findTop50BySentFalseOrderByUpdatedAtAsc();


    Optional<Notification> findByUserIdAndBookingId(Long userId, Long bookingId);

}

