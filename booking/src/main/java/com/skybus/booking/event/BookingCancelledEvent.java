package com.skybus.booking.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Published when a booking is cancelled.
 * Notification-service sends a cancellation confirmation email.
 * Payment-service can trigger a refund if paymentStatus was COMPLETED.
 */
public record BookingCancelledEvent(
        UUID          bookingId,
        String        bookingRef,
        UUID          userId,
        String        passengerEmail,
        String        passengerName,
        BigDecimal    totalAmount,
        LocalDateTime cancelledAt
) {}