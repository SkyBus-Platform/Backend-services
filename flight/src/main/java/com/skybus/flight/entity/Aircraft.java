package com.skybus.flight.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "skybus_aircraft",
        schema = "skybus_flight_schema"
)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Aircraft {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String model;

    @Column(name = "type_code", length = 10)
    private String typeCode;

    @Min(1)
    @Column(name = "economy_seats", nullable = false)
    private int economySeats;

    @Min(0)
    @Column(name = "business_seats", nullable = false)
    @Builder.Default
    private int businessSeats = 0;

    @Min(0)
    @Column(name = "first_class_seats", nullable = false)
    @Builder.Default
    private int firstClassSeats = 0;

    @OneToMany(mappedBy = "aircraft", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Flight> flights = new ArrayList<>();

    public int getTotalSeats() {
        return economySeats + businessSeats + firstClassSeats;
    }
}