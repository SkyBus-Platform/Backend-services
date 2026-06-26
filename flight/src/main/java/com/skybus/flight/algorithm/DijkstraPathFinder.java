package com.skybus.flight.algorithm;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Dijkstra's shortest-path algorithm adapted for multi-hop flight routes.
 *
 * Key adaptations vs textbook Dijkstra:
 *  1. Edges are time-ordered — a connecting flight must depart at least
 *     MIN_LAYOVER_MINUTES after the previous flight's arrival.
 *  2. The algorithm collects the top N cheapest/fastest routes rather than
 *     stopping at the first one found.
 *  3. Max hops is capped (default 3) to prevent combinatorial explosion.
 *
 * Time complexity: O((V + E) log V) with the priority queue.
 *
 * State tracked per queue entry:
 *   [totalCost, path (list of edges), currentNode, lastArrival]
 */
@Component
public class DijkstraPathFinder {

    private static final int    MIN_LAYOVER_MINUTES = 60;
    private static final int    MAX_RESULTS         = 5;
    private static final int    DEFAULT_MAX_HOPS    = 3;

    /**
     * @param graph       pre-built adjacency graph for the travel date
     * @param origin      departure IATA code (e.g. "CMB")
     * @param destination arrival IATA code  (e.g. "LHR")
     * @param maxHops     maximum number of flight legs (1 = direct only)
     * @return            up to MAX_RESULTS routes, ordered cheapest/fastest first
     */
    public List<List<FlightGraph.Edge>> findRoutes(
            FlightGraph graph,
            String origin,
            String destination,
            int maxHops) {

        if (maxHops <= 0) maxHops = DEFAULT_MAX_HOPS;

        List<List<FlightGraph.Edge>> results = new ArrayList<>();

        // Priority queue sorted by cumulative cost (min-heap)
        // Entry: [cost, path, currentNode, lastArrivalEpoch]
        PriorityQueue<State> pq = new PriorityQueue<>(
                Comparator.comparingDouble(s -> s.cost)
        );
        pq.offer(new State(0, new ArrayList<>(), origin, null, new ArrayList<>()));

        while (!pq.isEmpty() && results.size() < MAX_RESULTS) {
            State current = pq.poll();

            // Reached destination — record this route
            if (current.node.equals(destination) && !current.path.isEmpty()) {
                results.add(new ArrayList<>(current.path));
                continue;
            }

            // Prune — too many hops already
            if (current.path.size() >= maxHops) continue;

            for (FlightGraph.Edge edge : graph.edgesFrom(current.node)) {
                if (current.visited.contains(current.node)) continue;

                if (current.lastArrival != null &&
                        edge.getDeparture().isBefore(current.lastArrival)) continue;

                if (current.lastArrival != null) {
                    long minutesBetween = java.time.Duration
                            .between(current.lastArrival, edge.getDeparture())
                            .toMinutes();
                    if (minutesBetween < MIN_LAYOVER_MINUTES) continue;
                }

                List<String> newVisited = new ArrayList<>(current.visited);
                newVisited.add(current.node);

                List<FlightGraph.Edge> newPath = new ArrayList<>(current.path);
                newPath.add(edge);

                pq.offer(new State(
                        current.cost + edge.getWeight(),
                        newPath,
                        edge.getTo(),
                        edge.getArrival(),
                        newVisited
                ));
            }
        }

        return results;
    }

    // Internal state record
    private record State(
            double                   cost,
            List<FlightGraph.Edge>   path,
            String                   node,
            java.time.LocalDateTime  lastArrival,
            List<String>             visited
    ) {}
}