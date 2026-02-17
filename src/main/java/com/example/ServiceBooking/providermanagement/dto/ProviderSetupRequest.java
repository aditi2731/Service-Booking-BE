package com.example.ServiceBooking.providermanagement.dto;



import lombok.Data;
import java.util.List;

@Data
public class ProviderSetupRequest {
    private List<Long> subServiceIds;
}

