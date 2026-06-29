package com.skybus.booking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class SeatReservationException extends RuntimeException {
    public SeatReservationException(String message) {
        super(message);
    }
}