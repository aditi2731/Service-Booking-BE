package com.example.ServiceBooking.providermanagement.dto;



import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EarningsResponse {
    private int completedJobs;
    private double totalEarnings;
}
