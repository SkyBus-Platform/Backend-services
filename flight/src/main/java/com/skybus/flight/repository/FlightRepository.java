package com.skybus.flight.repository;

import com.skybus.flight.entity.Flight;
import com.skybus.flight.enums.FlightStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface FlightRepository extends JpaRepository<Flight, UUID> {

    /**
     * Core search query used by Dijkstra's graph builder.
     * Finds all bookable flights departing on a given date, with at least
     * one available seat. Results are eagerly joined to avoid N+1 on airports.
     */
    @Query("""
        SELECT f FROM Flight f
        JOIN FETCH f.departureAirport
        JOIN FETCH f.arrivalAirport
        JOIN FETCH f.airline
        WHERE f.status IN :statuses
          AND f.availableSeats > 0
          AND f.departureTime >= :start
          AND f.departureTime <= :end
        ORDER BY f.departureTime ASC
    """)
    List<Flight> findBookableFlightsForDate(
            @Param("statuses") List<FlightStatus> statuses,
            @Param("start")    LocalDateTime start,
            @Param("end")      LocalDateTime end
    );

    /**
     * Direct point-to-point search — used for simple single-hop display.
     */
    @Query("""
        SELECT f FROM Flight f
        JOIN FETCH f.departureAirport dep
        JOIN FETCH f.arrivalAirport arr
        JOIN FETCH f.airline
        WHERE dep.iataCode = :origin
          AND arr.iataCode = :destination
          AND f.availableSeats > 0
          AND f.status IN ('SCHEDULED', 'DELAYED')
          AND CAST(f.departureTime AS date) = :date
        ORDER BY f.departureTime ASC
    """)
    List<Flight> findDirectFlights(
            @Param("origin")      String origin,
            @Param("destination") String destination,
            @Param("date")        LocalDate date
    );
}