package com.example.ServiceBooking.providermanagement.dto;



import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
public class ProviderSetupRequest {
    private List<Long> subServiceIds;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;
}

