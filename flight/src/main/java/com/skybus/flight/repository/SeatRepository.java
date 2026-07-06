package com.skybus.flight.repository;

import com.skybus.flight.entity.Seat;
import com.skybus.flight.enums.SeatClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {

    @Query("""
        SELECT s
        FROM Seat s
        WHERE s.id = :seatId
    """)
    Optional<Seat> findSeatById(
            @Param("seatId") UUID seatId
    );

    /** All available seats for a flight, optionally filtered by class. */
    @Query("""
        SELECT s FROM Seat s
        WHERE s.flight.id = :flightId
          AND s.isBooked = false
          AND (:seatClass IS NULL OR s.seatClass = :seatClass)
        ORDER BY s.seatClass, s.seatNumber
    """)
    List<Seat> findAvailableSeats(
            @Param("flightId")  UUID flightId,
            @Param("seatClass") SeatClass seatClass
    );

    /** Used during booking — pessimistic lock prevents race condition. */
    @Query("SELECT s FROM Seat s WHERE s.id = :id AND s.isBooked = false")
    Optional<Seat> findAvailableSeatById(@Param("id") UUID id);

    List<Seat> findByFlightId(UUID flightId);
}