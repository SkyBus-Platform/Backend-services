package com.skybus.flight.dto.response;

import com.skybus.flight.entity.Seat;
import com.skybus.flight.enums.SeatClass;
import java.math.BigDecimal;
import java.util.UUID;

public record SeatResponse(
        UUID       id,
        String     seatNumber,
        SeatClass  seatClass,
        BigDecimal price,
        boolean    isBooked
) {
    public static SeatResponse from(Seat s) {
        return new SeatResponse(s.getId(), s.getSeatNumber(),
                s.getSeatClass(), s.getPrice(), s.isBooked());
    }
}