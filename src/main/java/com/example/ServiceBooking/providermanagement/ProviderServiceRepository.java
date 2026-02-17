package com.example.ServiceBooking.providermanagement;



import com.example.ServiceBooking.providermanagement.ProviderService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProviderServiceRepository extends JpaRepository<ProviderService, Long> {

    List<ProviderService> findByProvider_UserId(Long userId);

    @Query("select ps.provider.user.id from ProviderService ps where ps.subService.id = :subServiceId")
    List<Long> findProviderIdsBySubServiceId(Long subServiceId);
}
