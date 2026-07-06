package com.skybus.booking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class FlightServiceUnavailableException extends RuntimeException {
    public FlightServiceUnavailableException(String message) {
        super(message);
    }
}