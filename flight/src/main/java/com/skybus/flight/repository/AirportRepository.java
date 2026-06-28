package com.skybus.flight.repository;

import com.skybus.flight.entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AirportRepository extends JpaRepository<Airport, UUID> {
    Optional<Airport> findByIataCode(String iataCode);
    boolean existsByIataCode(String iataCode);
    List<Airport> findByCityContainingIgnoreCaseOrNameContainingIgnoreCase(String city, String name);
}