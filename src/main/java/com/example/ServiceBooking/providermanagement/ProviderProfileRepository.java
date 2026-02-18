package com.example.ServiceBooking.providermanagement;



import com.example.ServiceBooking.providermanagement.ProviderProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProviderProfileRepository extends JpaRepository<ProviderProfile, Long> {
    List<ProviderProfile> findByApprovedTrueAndOnlineTrue();
    List<ProviderProfile> findByCityAndApprovedTrueAndOnlineTrue(String city);
    @Query("select p.userId from ProviderProfile p where p.city = :city and p.approved = true and p.online = true")
    List<Long> findEligibleProviderIdsByCity(@Param("city") String city);
    Page<ProviderProfile> findAll(Pageable pageable);

    Optional<ProviderProfile> findByUserId(Long userId);
}

