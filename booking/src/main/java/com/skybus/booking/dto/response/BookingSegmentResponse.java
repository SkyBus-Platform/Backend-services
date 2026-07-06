package com.skybus.booking.dto.response;

import com.skybus.booking.entity.BookingSegment;
import com.skybus.booking.enums.SeatClass;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookingSegmentResponse(
        UUID          id,
        int           hopOrder,
        UUID          flightId,
        String        flightNumber,
        String        airlineCode,
        String        airlineName,
        String        originCode,
        String        originCity,
        String        destinationCode,
        String        destinationCity,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        UUID          seatId,
        String        seatNumber,
        SeatClass     seatClass,
        BigDecimal    price
) {
    public static BookingSegmentResponse from(BookingSegment s) {
        return new BookingSegmentResponse(
                s.getId(), s.getHopOrder(),
                s.getFlightId(), s.getFlightNumber(),
                s.getAirlineCode(), s.getAirlineName(),
                s.getOriginCode(), s.getOriginCity(),
                s.getDestinationCode(), s.getDestinationCity(),
                s.getDepartureTime(), s.getArrivalTime(),
                s.getSeatId(), s.getSeatNumber(),
                s.getSeatClass(), s.getPrice()
        );
    }
}