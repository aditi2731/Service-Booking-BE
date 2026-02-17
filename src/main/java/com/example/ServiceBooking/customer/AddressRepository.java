package com.example.ServiceBooking.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository
        extends JpaRepository<Address, Long> {

    List<Address> findByCustomer_CustomerId(Long customerId);
}

