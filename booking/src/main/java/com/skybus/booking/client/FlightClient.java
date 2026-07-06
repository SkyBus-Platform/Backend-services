package com.skybus.booking.client;

import com.skybus.booking.client.FlightClientDtos.*;
import com.skybus.booking.exception.FlightServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Feign client for flight-service.
 *
 * url is resolved from application.yml: services.flight.url
 * fallback kicks in when circuit is open or flight-service is down.
 *
 * Resilience4j wraps each call with a circuit breaker named "flight-service".
 * After 50% failure rate over 10 calls, the circuit opens for 10 seconds.
 */
@FeignClient(
        name     = "flight-service",
        url      = "${services.flight.url}"
//        fallback = FlightClient.FlightClientFallback.class
)
public interface FlightClient {

    @GetMapping("/api/flights/{id}")
    FlightSummary getFlight(@PathVariable UUID id);

    @GetMapping("/api/seats/{id}")
    SeatSummary getSeat(@PathVariable UUID id);

    /**
     * Atomically marks the seat as booked in flight-service.
     * Returns the updated seat. Throws 409 if already booked.
     */
    @PostMapping("/api/seats/{id}/reserve")
    SeatSummary reserveSeat(@PathVariable UUID id);

    /**
     * Releases the seat back to available — called on booking cancellation
     * or saga compensation (rollback).
     */
    @PostMapping("/api/seats/{id}/release")
    void releaseSeat(@PathVariable UUID id);

    // ── Circuit breaker fallback ──────────────────────────────────────────────
    @Slf4j
    class FlightClientFallback implements FlightClient {

        @Override
        public FlightSummary getFlight(UUID id) {
            log.error("Flight service unavailable — could not fetch flight {}", id);
            throw new FlightServiceUnavailableException("Flight service is currently unavailable");
        }

        @Override
        public SeatSummary getSeat(UUID id) {
            log.error("Flight service unavailable — could not fetch seat {}", id);
            throw new FlightServiceUnavailableException("Flight service is currently unavailable");
        }

        @Override
        public SeatSummary reserveSeat(UUID id) {
            log.error("Flight service unavailable — could not reserve seat {}", id);
            throw new FlightServiceUnavailableException("Flight service is currently unavailable. Please try again.");
        }

        @Override
        public void releaseSeat(UUID id) {
            // Best-effort — log and continue; a background job can retry later
            log.error("CRITICAL: Could not release seat {} — manual intervention may be needed", id);
        }
    }
}