package com.example.ServiceBooking.servicecatalog;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceCategoryRepository
        extends JpaRepository<ServiceCategory, Long> {

    Optional<ServiceCategory> findByNameIgnoreCase(String name);
}

