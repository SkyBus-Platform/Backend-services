package com.skybus.flight.service;

import com.skybus.flight.algorithm.DijkstraPathFinder;
import com.skybus.flight.algorithm.FlightGraph;
import com.skybus.flight.dto.request.SearchRouteRequest;
import com.skybus.flight.dto.response.FlightResponse;
import com.skybus.flight.dto.response.RouteResponse;
import com.skybus.flight.entity.Flight;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {

    private final FlightService      flightService;
    private final DijkstraPathFinder pathFinder;

    /**
     * Main entry point for route search.
     *
     * 1. Load all bookable flights for the travel date from DB.
     * 2. Build an in-memory weighted graph.
     * 3. Run Dijkstra's from origin to destination.
     * 4. Map each found path to a RouteResponse.
     */
    @Transactional(readOnly = true)
    public List<RouteResponse> search(SearchRouteRequest req) {
        boolean optimizeByPrice = "price".equals(req.optimize());

        log.info("Searching routes {} → {} on {} optimising by {}",
                req.origin(), req.destination(), req.date(), req.optimize());

        // Step 1 — load all candidate flights for this date
        List<Flight> flights = flightService.findBookableFlightsForDate(req.date());
        log.debug("Loaded {} bookable flights for {}", flights.size(), req.date());

        if (flights.isEmpty()) return List.of();

        // Step 2 — build graph
        FlightGraph graph = new FlightGraph();
        flights.forEach(f -> graph.addFlight(f, optimizeByPrice));

        // Step 3 — run Dijkstra's
        List<List<FlightGraph.Edge>> paths = pathFinder.findRoutes(
                graph,
                req.origin().toUpperCase(),
                req.destination().toUpperCase(),
                optimizeByPrice,
                req.maxHops()
        );

        log.info("Found {} routes from {} to {}", paths.size(), req.origin(), req.destination());

        // Step 4 — map to response DTOs
        return paths.stream().map(this::toRouteResponse).toList();
    }

    private RouteResponse toRouteResponse(List<FlightGraph.Edge> path) {
        List<FlightResponse> hops = path.stream()
                .map(edge -> FlightResponse.from(edge.getFlight()))
                .toList();

        BigDecimal totalPrice = path.stream()
                .map(e -> e.getFlight().getBasePrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total journey time = from first departure to last arrival
        long totalMinutes = Duration.between(
                path.getFirst().getDeparture(),
                path.getLast().getArrival()
        ).toMinutes();

        return new RouteResponse(path.size(), totalPrice, totalMinutes, hops);
    }
}