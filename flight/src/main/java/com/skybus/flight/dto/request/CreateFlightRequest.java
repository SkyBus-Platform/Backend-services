package com.skybus.flight.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateFlightRequest(
        @NotBlank @Size(max = 10)   String        flightNumber,
        @NotNull                    UUID          airlineId,
        @NotNull                    UUID          aircraftId,
        @NotBlank @Size(min=3,max=3) String       originCode,
        @NotBlank @Size(min=3,max=3) String       destinationCode,
        @NotNull                    LocalDateTime departureTime,
        @NotNull                    LocalDateTime arrivalTime,
        @NotNull @DecimalMin("0.01") BigDecimal   basePrice
) {}