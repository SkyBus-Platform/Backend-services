package com.skybus.booking.entity;

import com.skybus.booking.enums.SeatClass;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One flight hop within a booking.
 * Stores a complete snapshot of flight + seat data — no FK to flight-service.
 * hopOrder 1 = first leg, 2 = connection, etc.
 */
@Entity
@Table(
        name   = "skybus_booking_segment",
        schema = "skybus_booking_schema",
        indexes = {
                @Index(name = "idx_seg_booking_id",  columnList = "booking_id"),
                @Index(name = "idx_seg_flight_id",   columnList = "flight_id"),
                @Index(name = "idx_seg_seat_id",     columnList = "seat_id"),
                @Index(name = "idx_seg_origin",      columnList = "origin_code"),
                @Index(name = "idx_seg_dest",        columnList = "destination_code"),
                @Index(name = "idx_seg_dep_time",    columnList = "departure_time")
        }
)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BookingSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, updatable = false)
    private Booking booking;

    @Column(name = "hop_order", nullable = false, updatable = false)
    private int hopOrder;

    // ── Flight snapshot ───────────────────────────────────────────────────────
    @Column(name = "flight_id", nullable = false, updatable = false)
    private UUID flightId;

    @Column(name = "flight_number", nullable = false, length = 10)
    private String flightNumber;

    @Column(name = "airline_code", length = 3)
    private String airlineCode;

    @Column(name = "airline_name", length = 150)
    private String airlineName;

    // ── Route snapshot ────────────────────────────────────────────────────────
    @Column(name = "origin_code", nullable = false, length = 3)
    private String originCode;

    @Column(name = "origin_city", length = 100)
    private String originCity;

    @Column(name = "destination_code", nullable = false, length = 3)
    private String destinationCode;

    @Column(name = "destination_city", length = 100)
    private String destinationCity;

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    // ── Seat snapshot ─────────────────────────────────────────────────────────
    @Column(name = "seat_id", nullable = false, updatable = false)
    private UUID seatId;

    @Column(name = "seat_number", nullable = false, length = 4)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_class", nullable = false, length = 20)
    private SeatClass seatClass;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}