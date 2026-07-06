package com.skybus.booking.client;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTOs that mirror the flight-service response shapes.
 * Defined here so booking-service has zero compile-time dependency
 * on the flight-service module.
 */
public final class FlightClientDtos {

    private FlightClientDtos() {}

    public record AirportSummary(
            UUID   id,
            String iataCode,
            String name,
            String city,
            String country
    ) {}

    public record FlightSummary(
            UUID          id,
            String        flightNumber,
            String        airlineName,
            String        airlineCode,
            AirportSummary departureAirport,
            AirportSummary arrivalAirport,
            LocalDateTime  departureTime,
            LocalDateTime  arrivalTime,
            BigDecimal     basePrice,
            String         status,
            int            availableSeats
    ) {}

    public record SeatSummary(
            UUID       id,
            String     seatNumber,
            String     seatClass,
            BigDecimal price,
            boolean    isBooked
    ) {}
}