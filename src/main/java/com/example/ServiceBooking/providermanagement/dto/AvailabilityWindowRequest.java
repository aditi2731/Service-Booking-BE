package com.example.ServiceBooking.providermanagement.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record AvailabilityWindowRequest(
        @NotNull LocalDate fromDate,
        @NotNull LocalDate toDate,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime
) {}
