package com.example.ServiceBooking.providermanagement;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ProviderAvailabilityRepository extends JpaRepository<ProviderAvailability, Long> {

    List<ProviderAvailability> findByProviderIdAndDateAndStatusOrderByStartTimeAsc(
            Long providerId, LocalDate date, AvailabilityStatus status
    );

    @Query("""
        select a from ProviderAvailability a
        where a.providerId = :providerId
          and a.date = :date
          and a.startTime = :start
          and a.endTime = :end
          and a.status = com.example.ServiceBooking.providermanagement.AvailabilityStatus.AVAILABLE
    """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ProviderAvailability> lockAvailableSlot(
            Long providerId, LocalDate date, LocalTime start, LocalTime end
    );

    @Query("""
        select a from ProviderAvailability a
        where a.providerId in :providerIds
          and a.date = :date
          and a.status = com.example.ServiceBooking.providermanagement.AvailabilityStatus.AVAILABLE
        order by a.startTime asc
    """)
    List<ProviderAvailability> findAvailableSlotsForProviders(List<Long> providerIds, LocalDate date);

    boolean existsByProviderIdAndDate(Long providerId, LocalDate date);
    void deleteByProviderIdAndDate(Long providerId, LocalDate date);

}

