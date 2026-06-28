package com.skybus.flight.service;

import com.skybus.flight.dto.request.CreateFlightRequest;
import com.skybus.flight.dto.response.FlightResponse;
import com.skybus.flight.entity.*;
import com.skybus.flight.enums.FlightStatus;
import com.skybus.flight.enums.SeatClass;
import com.skybus.flight.exception.FlightNotFoundException;
import com.skybus.flight.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository   flightRepo;
    private final AirlineRepository  airlineRepo;
    private final AircraftRepository aircraftRepo;
    private final AirportService     airportService;
    private final SeatRepository     seatRepo;

    // ── Read ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public FlightResponse getById(UUID id) {
        return FlightResponse.from(findById(id));
    }

    @Transactional(readOnly = true)
    public List<FlightResponse> searchDirect(String origin, String destination, LocalDate date) {
        return flightRepo.findDirectFlights(origin.toUpperCase(), destination.toUpperCase(), date)
                .stream().map(FlightResponse::from).toList();
    }

    /**
     * Loads all bookable flights for a date — used by RouteService to build the graph.
     */
    @Transactional(readOnly = true)
    public List<Flight> findBookableFlightsForDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end   = date.atTime(LocalTime.MAX);
        return flightRepo.findBookableFlightsForDate(
                List.of(FlightStatus.SCHEDULED, FlightStatus.DELAYED), start, end);
    }

    // ── Create ──────────────────────────────────────────────────────────────

    @Transactional
    public FlightResponse create(CreateFlightRequest req) {
        Airport departure = airportService.findByIata(req.originCode());
        Airport arrival   = airportService.findByIata(req.destinationCode());

        Airline airline = airlineRepo.findById(req.airlineId())
                .orElseThrow(() -> new RuntimeException("Airline not found"));
        Aircraft aircraft = aircraftRepo.findById(req.aircraftId())
                .orElseThrow(() -> new RuntimeException("Aircraft not found"));

        Flight flight = Flight.builder()
                .flightNumber(req.flightNumber())
                .airline(airline)
                .aircraft(aircraft)
                .departureAirport(departure)
                .arrivalAirport(arrival)
                .departureTime(req.departureTime())
                .arrivalTime(req.arrivalTime())
                .basePrice(req.basePrice())
                .availableSeats(aircraft.getTotalSeats())
                .build();

        flight = flightRepo.save(flight);

        // Auto-generate seat rows based on aircraft configuration
        generateSeats(flight, aircraft, req.basePrice());
        log.info("Created flight {} with {} seats", flight.getFlightNumber(), aircraft.getTotalSeats());

        return FlightResponse.from(flight);
    }

    // ── Status update ────────────────────────────────────────────────────────

    @Transactional
    public FlightResponse updateStatus(UUID id, FlightStatus status) {
        Flight flight = findById(id);
        flight.setStatus(status);
        return FlightResponse.from(flightRepo.save(flight));
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    Flight findById(UUID id) {
        return flightRepo.findById(id)
                .orElseThrow(() -> new FlightNotFoundException(id.toString()));
    }

    private void generateSeats(Flight flight, Aircraft aircraft, BigDecimal basePrice) {

        // First class rows 1–(firstClassSeats/6)
        List<Seat> seats = new ArrayList<>(buildSeats(flight, 1, aircraft.getFirstClassSeats(),
                SeatClass.FIRST, basePrice.multiply(BigDecimal.valueOf(3.5))));

        // Business rows after first class
        int bizStart = aircraft.getFirstClassSeats() / 6 + 2;
        seats.addAll(buildSeats(flight, bizStart, aircraft.getBusinessSeats(),
                SeatClass.BUSINESS, basePrice.multiply(BigDecimal.valueOf(2.0))));

        // Economy fills the rest
        int ecoStart = bizStart + aircraft.getBusinessSeats() / 6 + 2;
        seats.addAll(buildSeats(flight, ecoStart, aircraft.getEconomySeats(),
                SeatClass.ECONOMY, basePrice));

        seatRepo.saveAll(seats);
    }

    private List<Seat> buildSeats(Flight flight, int startRow,
                                  int count, SeatClass cls, BigDecimal price) {
        List<Seat> seats = new ArrayList<>();
        String[] cols = {"A", "B", "C", "D", "E", "F"};
        int row = startRow;
        int built = 0;

        while (built < count) {
            for (String col : cols) {
                if (built >= count) break;
                seats.add(Seat.builder()
                        .flight(flight)
                        .seatNumber(row + col)
                        .seatClass(cls)
                        .price(price)
                        .build());
                built++;
            }
            row++;
        }
        return seats;
    }
}