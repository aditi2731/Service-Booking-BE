package com.example.ServiceBooking.auth.dto;

import com.example.ServiceBooking.auth.Role;
import com.example.ServiceBooking.auth.Status;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        Status status
) {}

