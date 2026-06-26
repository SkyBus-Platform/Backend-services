package com.skybus.flight.dto.response;

import com.skybus.flight.entity.Flight;
import com.skybus.flight.enums.FlightStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FlightResponse(
        UUID          id,
        String        flightNumber,
        String        airlineName,
        String        airlineCode,
        AirportResponse departureAirport,
        AirportResponse arrivalAirport,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        long          durationMinutes,
        BigDecimal    basePrice,
        FlightStatus  status,
        int           availableSeats
) {
    public static FlightResponse from(Flight f) {
        return new FlightResponse(
                f.getId(),
                f.getFlightNumber(),
                f.getAirline().getName(),
                f.getAirline().getIataCode(),
                AirportResponse.from(f.getDepartureAirport()),
                AirportResponse.from(f.getArrivalAirport()),
                f.getDepartureTime(),
                f.getArrivalTime(),
                f.getDurationMinutes(),
                f.getBasePrice(),
                f.getStatus(),
                f.getAvailableSeats()
        );
    }
}