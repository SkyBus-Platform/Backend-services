package com.skybus.flight.controller;

import com.skybus.flight.dto.request.SearchRouteRequest;
import com.skybus.flight.dto.response.RouteResponse;
import com.skybus.flight.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    /**
     * Multi-hop route search using Dijkstra's algorithm.
     *
     * GET /api/routes/search
     *   ?origin=CMB
     *   &destination=LHR
     *   &date=2025-12-20
     *   &optimize=price        (or "duration")
     *   &maxHops=3
     *
     * Returns up to 5 ranked routes, each with all flight hops.
     */
    @GetMapping("/search")
    public ResponseEntity<List<RouteResponse>> search(@Valid SearchRouteRequest req) {
        return ResponseEntity.ok(routeService.search(req));
    }
}