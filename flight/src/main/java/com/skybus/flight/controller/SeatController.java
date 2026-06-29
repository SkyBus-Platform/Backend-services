package com.skybus.flight.controller;

import com.skybus.flight.dto.response.SeatResponse;
import com.skybus.flight.enums.SeatClass;
import com.skybus.flight.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @GetMapping("/{id}")
    public ResponseEntity<SeatResponse> getSeat(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(seatService.getSeat(id));
    }

    @GetMapping("/flight/{flightId}")
    public ResponseEntity<List<SeatResponse>> getAvailable(
            @PathVariable UUID flightId,
            @RequestParam(required = false) SeatClass seatClass) {
        return ResponseEntity.ok(seatService.getAvailableSeats(flightId, seatClass));
    }

    @GetMapping("/flight/{flightId}/all")
    public ResponseEntity<List<SeatResponse>> getAll(@PathVariable UUID flightId) {
        return ResponseEntity.ok(seatService.getAllSeats(flightId));
    }

    /** Called by booking-service to reserve a seat. */
    @PostMapping("/{id}/reserve")
    public ResponseEntity<SeatResponse> reserve(@PathVariable UUID id) {
        return ResponseEntity.ok(seatService.reserve(id));
    }

    /** Called by booking-service when a booking is cancelled. */
    @PostMapping("/{id}/release")
    public ResponseEntity<Void> release(@PathVariable UUID id) {
        seatService.release(id);
        return ResponseEntity.noContent().build();
    }
}