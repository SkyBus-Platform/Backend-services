package com.skybus.flight.repository;

import com.skybus.flight.entity.Airline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AirlineRepository extends JpaRepository<Airline, UUID> {
    Optional<Airline> findByIataCode(String iataCode);
    boolean existsByIataCode(String iataCode);
}