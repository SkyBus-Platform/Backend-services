package com.skybus.flight.controller;

import com.skybus.flight.dto.request.CreateFlightRequest;
import com.skybus.flight.dto.response.FlightResponse;
import com.skybus.flight.enums.FlightStatus;
import com.skybus.flight.service.FlightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @GetMapping("/{id}")
    public ResponseEntity<FlightResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(flightService.getById(id));
    }

    /**
     * Direct point-to-point search.
     * For multi-hop optimised search use GET /api/routes/search instead.
     */
    @GetMapping("/search")
    public ResponseEntity<List<FlightResponse>> searchDirect(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(flightService.searchDirect(origin, destination, date));
    }

    @PostMapping
    public ResponseEntity<FlightResponse> create(@Valid @RequestBody CreateFlightRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(flightService.create(req));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<FlightResponse> updateStatus(
            @PathVariable UUID id,
            @RequestParam FlightStatus status) {
        return ResponseEntity.ok(flightService.updateStatus(id, status));
    }
}