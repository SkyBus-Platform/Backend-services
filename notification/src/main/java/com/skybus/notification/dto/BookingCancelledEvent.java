package com.skybus.notification.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mirrors BookingCancelledEvent from booking-service.
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