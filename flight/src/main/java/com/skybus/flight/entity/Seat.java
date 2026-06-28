package com.skybus.flight.entity;

import com.skybus.flight.enums.SeatClass;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name   = "skybus_seat",
        schema = "skybus_flight_schema",
        indexes = {
                @Index(name = "idx_seat_flight",         columnList = "flight_id"),
                @Index(name = "idx_seat_flight_booked",  columnList = "flight_id,is_booked"),
                @Index(name = "idx_seat_flight_class",   columnList = "flight_id,seat_class,is_booked")
        },
        uniqueConstraints = @UniqueConstraint(
                name        = "uq_seat_flight_number",
                columnNames = {"flight_id", "seat_number"}
        )
)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flight_id", nullable = false, updatable = false)
    private Flight flight;

    @NotBlank
    @Size(max = 4)
    @Column(name = "seat_number", nullable = false, length = 4)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_class", nullable = false, length = 20)
    private SeatClass seatClass;

    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "is_booked", nullable = false)
    @Builder.Default
    private boolean isBooked = false;

    /**
     * Optimistic lock — Hibernate increments this on every UPDATE.
     * Prevents two requests booking the same seat simultaneously.
     * The second writer gets OptimisticLockException → 409 Conflict.
     */
    @Version
    @Column(nullable = false)
    private Long version;

    // Helpers
    public void reserve() {
        if (isBooked) throw new IllegalStateException("Seat " + seatNumber + " is already booked");
        this.isBooked = true;
    }

    public void release() {
        this.isBooked = false;
    }
}