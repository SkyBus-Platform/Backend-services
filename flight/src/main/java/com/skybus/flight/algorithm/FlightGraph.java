package com.skybus.flight.algorithm;

import com.skybus.flight.entity.Flight;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Models the airport network as a directed weighted graph.
 *
 * Vertex  = airport IATA code (String)
 * Edge    = Flight (departure → arrival, weighted by price or duration)
 *
 * Built fresh for every search request from the list of bookable flights
 * on that travel date. Kept in-memory only — not persisted.
 */
public class FlightGraph {

    /** One edge in the graph — wraps a Flight with its computed weight. */
    @Getter
    public static class Edge {
        private final Flight        flight;
        private final String        from;
        private final String        to;
        private final double        weight;   // price (USD) or duration (minutes)
        private final LocalDateTime departure;
        private final LocalDateTime arrival;

        public Edge(Flight flight, boolean optimizeByPrice) {
            this.flight   = flight;
            this.from     = flight.getDepartureAirport().getIataCode();
            this.to       = flight.getArrivalAirport().getIataCode();
            this.departure = flight.getDepartureTime();
            this.arrival   = flight.getArrivalTime();
            this.weight    = optimizeByPrice
                    ? flight.getBasePrice().doubleValue()
                    : flight.getDurationMinutes();
        }
    }

    // iataCode → outbound edges
    private final Map<String, List<Edge>> adjacency = new HashMap<>();

    public void addFlight(Flight flight, boolean optimizeByPrice) {
        String from = flight.getDepartureAirport().getIataCode();
        adjacency.computeIfAbsent(from, k -> new ArrayList<>())
                .add(new Edge(flight, optimizeByPrice));
    }

    public List<Edge> edgesFrom(String iataCode) {
        return adjacency.getOrDefault(iataCode, Collections.emptyList());
    }

    public Set<String> allNodes() {
        return adjacency.keySet();
    }
}