package com.skybus.flight.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name   = "skybus_airport",
        schema = "skybus_flight_schema",
        indexes = {
                @Index(name = "idx_airport_iata", columnList = "iata_code", unique = true),
                @Index(name = "idx_airport_city", columnList = "city")
        }
)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Airport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotBlank
    @Size(min = 3, max = 3)
    @Column(name = "iata_code", nullable = false, unique = true, length = 3)
    private String iataCode;

    @Size(min = 4, max = 4)
    @Column(name = "icao_code", length = 4)
    private String icaoCode;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String name;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String city;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String country;

    @Column(length = 50)
    private String timezone;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @OneToMany(mappedBy = "departureAirport", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Flight> departingFlights = new ArrayList<>();

    @OneToMany(mappedBy = "arrivalAirport", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Flight> arrivingFlights = new ArrayList<>();
}