package com.skybus.flight.entity;

import com.skybus.flight.enums.FlightStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name   = "skybus_flight",
        schema = "skybus_flight_schema",
        indexes = {
                @Index(name = "idx_flight_dep_airport",  columnList = "departure_airport_id"),
                @Index(name = "idx_flight_arr_airport",  columnList = "arrival_airport_id"),
                @Index(name = "idx_flight_dep_time",     columnList = "departure_time"),
                @Index(name = "idx_flight_status",       columnList = "status"),
                @Index(name = "idx_flight_search",       columnList = "departure_airport_id,departure_time,status")
        },
        uniqueConstraints = @UniqueConstraint(
                name        = "uq_flight_number_dep_time",
                columnNames = {"flight_number", "departure_time"}
        )
)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotBlank
    @Size(max = 10)
    @Column(name = "flight_number", nullable = false, length = 10)
    private String flightNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "airline_id", nullable = false)
    private Airline airline;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aircraft_id", nullable = false)
    private Aircraft aircraft;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "departure_airport_id", nullable = false)
    private Airport departureAirport;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "arrival_airport_id", nullable = false)
    private Airport arrivalAirport;

    @NotNull
    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @NotNull
    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    @NotNull
    @DecimalMin("0.01")
    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private FlightStatus status = FlightStatus.SCHEDULED;

    @Column(name = "available_seats", nullable = false)
    @Builder.Default
    private int availableSeats = 0;

    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Seat> seats = new ArrayList<>();

    // ── Helpers ──────────────────────────────────────────────
    public long getDurationMinutes() {
        return Duration.between(departureTime, arrivalTime).toMinutes();
    }

    public boolean isBookable() {
        return (status == FlightStatus.SCHEDULED || status == FlightStatus.DELAYED)
                && availableSeats > 0;
    }

    public void decrementAvailableSeats() {
        if (availableSeats <= 0) throw new IllegalStateException("No seats available");
        this.availableSeats--;
    }

    public void incrementAvailableSeats() {
        this.availableSeats++;
    }
}