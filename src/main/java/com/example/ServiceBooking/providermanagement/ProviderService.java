package com.example.ServiceBooking.providermanagement;

import com.example.ServiceBooking.servicecatalog.SubService;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "provider_services")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private ProviderProfile provider;

    @ManyToOne
    private SubService subService;
}

