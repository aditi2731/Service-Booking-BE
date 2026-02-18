package com.example.ServiceBooking.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.example.ServiceBooking.payment.PaymentStatus;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByBookingId(Long bookingId);

    Page<Payment> findAllByOrderByPaymentIdDesc(Pageable pageable);

    long countByStatus(PaymentStatus status);

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.status = :status")
    BigDecimal sumAmountByStatus(PaymentStatus status);

    @Query("""
        select count(p) from Payment p
        where p.status = :status
          and p.bookingId in (select b.id from Booking b where b.city = :city)
    """)
    long countByStatusAndCity(@Param("status") PaymentStatus status, @Param("city") String city);

    @Query("""
        select coalesce(sum(p.amount), 0) from Payment p
        where p.status = :status
          and p.bookingId in (select b.id from Booking b where b.city = :city)
    """)
    BigDecimal sumAmountByStatusAndCity(@Param("status") PaymentStatus status, @Param("city") String city);


    //analytics
    @Query("select sum(p.amount) from Payment p where p.status=:status")
    BigDecimal sumByStatus(PaymentStatus status);

}
