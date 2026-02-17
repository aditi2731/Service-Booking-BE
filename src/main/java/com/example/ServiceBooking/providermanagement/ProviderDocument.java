package com.example.ServiceBooking.providermanagement;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "provider_documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String documentType; // PAN, AADHAR, LICENSE
    private String documentUrl;

    @ManyToOne
    @JoinColumn(name = "provider_id")
    private ProviderProfile provider;
}

