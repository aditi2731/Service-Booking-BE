package com.example.ServiceBooking.bookings;

import com.example.ServiceBooking.bookings.dto.BookingStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    List<Booking> findByCustomerId(Long customerId);

    List<Booking> findByProviderId(Long providerId);

    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByStatusAndCity(BookingStatus status, String city);

    Optional<Booking> findByIdAndStatus(Long id, BookingStatus status);

    List<Booking> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Booking> findByProviderIdOrderByCreatedAtDesc(Long providerId);

    List<Booking> findByProviderIdAndStatus(Long providerId, BookingStatus status);

    Page<Booking> findAll(Specification<Booking> spec, Pageable finalPageable);

//    @Modifying
//    @Query("""
//    UPDATE Booking b
//    SET b.providerId = :providerId,
//        b.status = com.example.ServiceBooking.bookings.dto.BookingStatus.ACCEPTED
//    WHERE b.id = :bookingId
//      AND b.status = com.example.ServiceBooking.bookings.dto.BookingStatus.PENDING
//""")
//    int assignIfPending(Long bookingId, Long providerId);


    @Modifying
    @Query("""
    UPDATE Booking b
    SET b.providerId = :providerId,
        b.status = :accepted
    WHERE b.id = :bookingId
      AND b.status = :pending
""")
    int assignIfPending(
            @Param("bookingId") Long bookingId,
            @Param("providerId") Long providerId,
            @Param("pending") BookingStatus pending,
            @Param("accepted") BookingStatus accepted
    );


    Page<Booking> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(BookingStatus status);
    long countByCity(String city);
    long countByStatusAndCity(BookingStatus status, String city);

    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
    long countByStatusAndCreatedAtBetween(BookingStatus status, LocalDateTime from, LocalDateTime to);
    long countByCreatedAtBetweenAndCity(LocalDateTime from, LocalDateTime to, String city);
    long countByStatusAndCreatedAtBetweenAndCity(BookingStatus status, LocalDateTime from, LocalDateTime to, String city);

    long countByProviderIdAndStatus(Long providerId, BookingStatus status);

    @Query("select coalesce(sum(b.price), 0) from Booking b where b.providerId = :providerId and b.status = :status")
    BigDecimal sumPriceByProviderIdAndStatus(Long providerId, BookingStatus status);


    @Query("select count(b) from Booking b where year(b.createdAt)=:year and month(b.createdAt)=:month")
    long countByMonth(int year, int month);

    @Query("select count(b) from Booking b where year(b.createdAt)=:year and month(b.createdAt)=:month and b.status=:status")
    long countByMonthAndStatus(int year, int month, BookingStatus status);

    @Query("""
select b.providerId as providerId, count(b) as completedBookings
from Booking b
where b.status='COMPLETED'
group by b.providerId
""")
    Page<ProviderActivityProjection> providerActivity(Pageable pageable);

    @Query("""
select b.customerId as customerId, count(b) as totalBookings
from Booking b
group by b.customerId
""")
    Page<CustomerTrendProjection> customerUsage(Pageable pageable);

    boolean existsByProviderIdAndDateTimeBetweenAndStatusIn(Long providerId, @NotNull LocalDateTime localDateTime, LocalDateTime localDateTime1, List<BookingStatus> accepted);


    public interface ProviderActivityProjection {
        Long getProviderId();
        long getCompletedBookings();
    }

    public interface CustomerTrendProjection {
        Long getCustomerId();
        long getTotalBookings();
    }


}

