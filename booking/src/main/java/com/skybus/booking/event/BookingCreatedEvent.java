package com.skybus.booking.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Published to RabbitMQ after a booking is successfully created.
 * Consumed by notification-service to send confirmation email.
 *
 * Contains all data needed by consumers — they should NOT need to
 * call back to booking-service to enrich this event.
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
            java.math.BigDecimal price
    ) {}
}