package com.skybus.flight.service;

import com.skybus.flight.dto.response.SeatResponse;
import com.skybus.flight.entity.Flight;
import com.skybus.flight.entity.Seat;
import com.skybus.flight.enums.SeatClass;
import com.skybus.flight.exception.SeatNotAvailableException;
import com.skybus.flight.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepo;
    private final FlightService  flightService;

    @Transactional(readOnly = true)
    public List<SeatResponse> getAvailableSeats(UUID flightId, SeatClass seatClass) {
        return seatRepo.findAvailableSeats(flightId, seatClass)
                .stream().map(SeatResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<SeatResponse> getAllSeats(UUID flightId) {
        return seatRepo.findByFlightId(flightId)
                .stream().map(SeatResponse::from).toList();
    }

    /**
     * Called by booking-service via REST.
     * The @Version on Seat ensures only one concurrent request succeeds —
     * the second gets OptimisticLockException → 409.
     */
    @Transactional
    public SeatResponse reserve(UUID seatId) {
        Seat seat = seatRepo.findAvailableSeatById(seatId)
                .orElseThrow(() -> new SeatNotAvailableException(seatId.toString()));

        seat.reserve();
        seatRepo.save(seat);

        // Decrement cached counter on flight
        Flight flight = seat.getFlight();
        flight.decrementAvailableSeats();

        log.info("Seat {} reserved on flight {}", seat.getSeatNumber(), flight.getFlightNumber());
        return SeatResponse.from(seat);
    }

    /**
     * Called when a booking is cancelled — makes the seat available again.
     */
    @Transactional
    public void release(UUID seatId) {
        Seat seat = seatRepo.findById(seatId)
                .orElseThrow(() -> new SeatNotAvailableException(seatId.toString()));

        seat.release();
        seatRepo.save(seat);
        seat.getFlight().incrementAvailableSeats();

        log.info("Seat {} released on flight {}", seat.getSeatNumber(),
                seat.getFlight().getFlightNumber());
    }
}