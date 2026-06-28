package com.skybus.flight.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Represents one complete route from origin → destination.
 * May contain 1 (direct) or more (connecting) flight legs.
 *
 * totalPrice    — sum of basePrice across all hops
 * totalMinutes  — total journey time including layovers
 * hops          — ordered list of flights to take
 */
public record RouteResponse(
        int              hopCount,
        BigDecimal       totalPrice,
        long             totalMinutes,
        List<FlightResponse> hops
) {}