package com.skybus.booking.repository;

import com.skybus.booking.entity.Booking;
import com.skybus.booking.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    /**
     * Load booking with all segments in one query — avoids N+1 on segments list.
     */
    @Query("""
        SELECT b FROM Booking b
        LEFT JOIN FETCH b.segments s
        WHERE b.id = :id
    """)
    Optional<Booking> findByIdWithSegments(@Param("id") UUID id);

    /**
     * All bookings for a user, newest first, with segments loaded.
     */
    @Query("""
        SELECT DISTINCT b FROM Booking b
        LEFT JOIN FETCH b.segments
        WHERE b.userId = :userId
        ORDER BY b.createdAt DESC
    """)
    List<Booking> findByUserIdWithSegments(@Param("userId") UUID userId);

    List<Booking> findByUserIdAndStatus(UUID userId, BookingStatus status);

    boolean existsByBookingRef(String bookingRef);

    Optional<Booking> findByBookingRef(String bookingRef);
}