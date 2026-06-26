package com.skybus.flight.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "skybus_airline", schema = "skybus_flight_schema",
        indexes = @Index(name = "idx_airline_iata", columnList = "iata_code", unique = true))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Airline {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotBlank
    @Size(min = 2, max = 3)
    @Column(name = "iata_code", nullable = false, unique = true, length = 3)
    private String iataCode;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 100)
    private String country;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @OneToMany(mappedBy = "airline", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Flight> flights = new ArrayList<>();
}