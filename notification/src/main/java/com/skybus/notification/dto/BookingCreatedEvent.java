package com.skybus.notification.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Mirrors BookingCreatedEvent from booking-service.
 * Defined locally — no compile-time dependency on booking-service module.
 */
public record BookingCreatedEvent(
        UUID          bookingId,
        String        bookingRef,
        UUID          userId,
        String        passengerEmail,
        String        passengerName,
        BigDecimal    totalAmount,
        String        currency,
        LocalDateTime createdAt,
        List<SegmentInfo> segments
) {
    public record SegmentInfo(
            int           hopOrder,
            String        flightNumber,
            String        airlineCode,
            String        originCode,
            String        originCity,
            String        destinationCode,
            String        destinationCity,
            LocalDateTime departureTime,
            LocalDateTime arrivalTime,
            String        seatNumber,
            String        seatClass,
            BigDecimal    price
    ) {}
}