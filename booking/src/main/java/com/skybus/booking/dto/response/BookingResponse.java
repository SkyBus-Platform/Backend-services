package com.skybus.booking.dto.response;

import com.skybus.booking.entity.Booking;
import com.skybus.booking.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record BookingResponse(
        UUID                         id,
        String                       bookingRef,
        UUID                         userId,
        String                       passengerFirstName,
        String                       passengerLastName,
        String                       passengerEmail,
        BigDecimal                   totalAmount,
        String                       currency,
        BookingStatus                status,
        LocalDateTime                createdAt,
        LocalDateTime                cancelledAt,
        List<BookingSegmentResponse> segments
) {
    public static BookingResponse from(Booking b) {
        return new BookingResponse(
                b.getId(),
                b.getBookingRef(),
                b.getUserId(),
                b.getPassengerFirstName(),
                b.getPassengerLastName(),
                b.getPassengerEmail(),
                b.getTotalAmount(),
                b.getCurrency(),
                b.getStatus(),
                b.getCreatedAt(),
                b.getCancelledAt(),
                b.getSegments().stream()
                        .map(BookingSegmentResponse::from)
                        .toList()
        );
    }
}