package com.example.ServiceBooking.servicecatalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubServiceRepository
        extends JpaRepository<SubService, Long> {

    List<SubService> findByCategory_Id(Long categoryId);

    List<SubService> findByNameContainingIgnoreCase(String keyword);

    @Query("select s.id from SubService s where s.category.id = :categoryId")
    List<Long> findSubServiceIdsByCategoryId(@Param("categoryId") Long categoryId);
}

