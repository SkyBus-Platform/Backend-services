package com.skybus.flight.dto.response;

import com.skybus.flight.entity.Airport;

import java.math.BigDecimal;
import java.util.UUID;

public record AirportResponse(
        UUID   id,
        String iataCode,
        String icaoCode,
        String name,
        String city,
        String country,
        String timezone,
        BigDecimal latitude,
        BigDecimal longitude
) {
    public static AirportResponse from(Airport a) {
        return new AirportResponse(a.getId(), a.getIataCode(), a.getIcaoCode(),
                a.getName(), a.getCity(), a.getCountry(),
                a.getTimezone(), a.getLatitude(), a.getLongitude());
    }
}