package com.skybus.booking.enums;

/**
 * Booking lifecycle.
 *
 * PENDING    → seats reserved, payment not yet confirmed
 * CONFIRMED  → payment successful, boarding passes can be issued
 * CANCELLED  → user or system cancelled; seats released back to flight-service
 * COMPLETED  → all flights departed; historical record only
 */
public enum BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED
}