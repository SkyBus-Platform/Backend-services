package com.skybus.booking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class BookingCancellationException extends RuntimeException {
    public BookingCancellationException(String message) {
        super(message);
    }
}