package com.skybus.booking.controller;

import com.skybus.booking.dto.request.CreateBookingRequest;
import com.skybus.booking.dto.response.BookingResponse;
import com.skybus.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * All endpoints require the gateway to have validated the JWT and forwarded:
 *   X-User-Id   — UUID of the authenticated user
 *   X-User-Role — PASSENGER | ADMIN
 *
 * No JWT validation happens here — the gateway is the single point of auth.
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * Create a new booking.
     * Reserves seats in flight-service and persists a booking record.
     *
     * POST /api/bookings
     * Body: { segments: [{flightId, seatId}...], passengerFirstName, ... }
     */
    @PostMapping
    public ResponseEntity<BookingResponse> create(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CreateBookingRequest req) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bookingService.create(req, userId));
    }

    /**
     * Get all bookings for the authenticated user.
     *
     * GET /api/bookings
     */
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(bookingService.getByUser(userId));
    }

    /**
     * Get a specific booking by ID.
     * Returns 404 if the booking belongs to a different user (ownership privacy).
     *
     * GET /api/bookings/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getById(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.getById(id, userId));
    }

    /**
     * Cancel a booking.
     * Releases all reserved seats back to flight-service.
     *
     * DELETE /api/bookings/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<BookingResponse> cancel(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.cancel(id, userId));
    }
}