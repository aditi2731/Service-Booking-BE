package com.example.ServiceBooking.support.dto;



import com.example.ServiceBooking.auth.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSupportAgentRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        @NotNull Role role // must be SUPPORT_*
) {}

