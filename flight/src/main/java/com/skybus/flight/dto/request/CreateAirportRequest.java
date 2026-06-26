package com.skybus.flight.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateAirportRequest(
        @NotBlank @Size(min = 3, max = 3) String iataCode,
        @Size(min = 4, max = 4)           String icaoCode,
        @NotBlank                         String name,
        @NotBlank                         String city,
        @NotBlank                         String country,
        String timezone,
        @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude
) {}