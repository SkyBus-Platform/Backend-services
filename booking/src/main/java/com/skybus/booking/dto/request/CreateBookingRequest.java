package com.skybus.booking.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.UUID;

/**
 * Request body for POST /api/bookings.
 *
 * segments — ordered list of flight hops (1 = direct, 2+ = connecting).
 *            The caller must order them correctly — hopOrder is taken
 *            from the list index + 1.
 *
 * Passenger info is accepted in the request because the gateway has already
 * validated the JWT. The frontend has the user's name from the access token
 * claims and includes it here so booking-service doesn't need to call
 * user-service on every booking.
 */
public record CreateBookingRequest(

        @NotNull(message = "Passenger first name is required")
        @Size(max = 100)
        String passengerFirstName,

        @NotNull(message = "Passenger last name is required")
        @Size(max = 100)
        String passengerLastName,

        @Email(message = "A valid email is required")
        @NotBlank
        String passengerEmail,

        @Size(max = 20)
        String passengerPhone,

        @NotEmpty(message = "At least one flight segment is required")
        @Size(max = 4, message = "Maximum 4 hops per booking")
        @Valid
        List<SegmentRequest> segments
) {
    public record SegmentRequest(

            @NotNull(message = "Flight ID is required")
            UUID flightId,

            @NotNull(message = "Seat ID is required")
            UUID seatId
    ) {}
}