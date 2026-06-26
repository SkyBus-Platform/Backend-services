package com.skybus.flight.controller;

import com.skybus.flight.dto.request.CreateAirportRequest;
import com.skybus.flight.dto.response.AirportResponse;
import com.skybus.flight.service.AirportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/airports")
@RequiredArgsConstructor
public class AirportController {

    private final AirportService airportService;

    @GetMapping
    public ResponseEntity<List<AirportResponse>> getAll() {
        return ResponseEntity.ok(airportService.getAll());
    }

    @GetMapping("/{iataCode}")
    public ResponseEntity<AirportResponse> getByIata(@PathVariable String iataCode) {
        return ResponseEntity.ok(airportService.getByIata(iataCode));
    }

    @GetMapping("/search")
    public ResponseEntity<List<AirportResponse>> search(@RequestParam String q) {
        return ResponseEntity.ok(airportService.search(q));
    }

    @PostMapping
    public ResponseEntity<AirportResponse> create(@Valid @RequestBody CreateAirportRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(airportService.create(req));
    }
}