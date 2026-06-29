package com.skybus.booking.service;

import com.skybus.booking.client.FlightClient;
import com.skybus.booking.client.FlightClientDtos.*;
import com.skybus.booking.dto.request.CreateBookingRequest;
import com.skybus.booking.dto.response.BookingResponse;
import com.skybus.booking.entity.Booking;
import com.skybus.booking.entity.BookingSegment;
import com.skybus.booking.entity.Payment;
import com.skybus.booking.enums.BookingStatus;
import com.skybus.booking.enums.PaymentStatus;
import com.skybus.booking.enums.SeatClass;
import com.skybus.booking.exception.BookingCancellationException;
import com.skybus.booking.exception.BookingNotFoundException;
import com.skybus.booking.exception.SeatReservationException;
import com.skybus.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepo;
    private final FlightClient      flightClient;
    private final EventPublisher    eventPublisher;

    private static final String BOOKING_REF_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int    BOOKING_REF_LENGTH = 6;
    private final SecureRandom  secureRandom = new SecureRandom();

    // ── Create ─────────────────────────────────────────────────────────────────

    /**
     * Saga — creates a booking with seat reservation and compensation.
     *
     * Steps (in order):
     *  1. Reserve seats in flight-service (external calls, not in local tx).
     *  2. Persist the booking record with full data snapshot (local tx).
     *  3. Publish booking.created event to RabbitMQ (best-effort).
     *
     * Compensation:
     *  If any seat reservation fails, all previously reserved seats are
     *  released before throwing the exception to the caller.
     *
     * Why are Feign calls outside the @Transactional?
     *  Seat reservations are in flight-service's DB — they are NOT part of
     *  the local PostgreSQL transaction. If we put them inside @Transactional
     *  and the local commit fails, the seats would still be reserved in
     *  flight-service. Handling them outside the local transaction and then
     *  compensating on failure gives us correct behaviour.
     */
    public BookingResponse create(CreateBookingRequest req, UUID userId) {
        log.info("Creating booking for user {} with {} segment(s)", userId, req.segments().size());

        List<UUID> reservedSeatIds = new ArrayList<>();

        try {
            // ── Step 1: Gather flight/seat data and reserve seats ─────────────
            List<SegmentData> segmentDataList = buildAndReserveSegments(
                    req.segments(), reservedSeatIds
            );

            // ── Step 2: Persist booking (local transaction) ───────────────────
            Booking booking = persistBooking(req, userId, segmentDataList);

            // ── Step 3: Publish event (best-effort, non-blocking) ─────────────
            eventPublisher.publishBookingCreated(booking);

            log.info("Booking created successfully — ref: {}", booking.getBookingRef());
            return BookingResponse.from(booking);

        } catch (SeatReservationException e) {
            // ── Compensation: release all already-reserved seats ──────────────
            compensate(reservedSeatIds);
            throw e;
        } catch (RuntimeException e) {
            compensate(reservedSeatIds);
            throw e;
        }
    }

    // ── Read ───────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public BookingResponse getById(UUID bookingId, UUID userId) {
        Booking booking = bookingRepo.findByIdWithSegments(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        verifyOwnership(booking, userId);
        return BookingResponse.from(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getByUser(UUID userId) {
        return bookingRepo.findByUserIdWithSegments(userId)
                .stream()
                .map(BookingResponse::from)
                .toList();
    }

    // ── Cancel ─────────────────────────────────────────────────────────────────

    /**
     * Cancels a booking and releases all reserved seats back to flight-service.
     * Seat release is best-effort — if flight-service is down, we log the failure
     * but still cancel the booking so the user isn't stuck. A retry mechanism
     * (scheduled job or dead-letter queue) can handle orphaned seat reservations.
     */
    @Transactional
    public BookingResponse cancel(UUID bookingId, UUID userId) {
        Booking booking = bookingRepo.findByIdWithSegments(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        verifyOwnership(booking, userId);

        if (booking.getStatus() == BookingStatus.CANCELLED)
            throw new BookingCancellationException("Booking " + booking.getBookingRef() + " is already cancelled");

        if (booking.getStatus() == BookingStatus.COMPLETED)
            throw new BookingCancellationException("Completed bookings cannot be cancelled");

        // Release seats in flight-service (best-effort)
        booking.getSegments().forEach(seg -> {
            try {
                flightClient.releaseSeat(seg.getSeatId());
                log.debug("Released seat {} for flight {}", seg.getSeatNumber(), seg.getFlightNumber());
            } catch (Exception e) {
                log.error("WARN: Failed to release seat {} (flight {}) — may need manual cleanup",
                        seg.getSeatId(), seg.getFlightNumber(), e);
            }
        });

        // Update booking status
        booking.cancel();
        bookingRepo.save(booking);

        // Publish cancellation event
        eventPublisher.publishBookingCancelled(booking);

        log.info("Booking {} cancelled by user {}", booking.getBookingRef(), userId);
        return BookingResponse.from(booking);
    }

    // ── Internal helpers ───────────────────────────────────────────────────────

    /**
     * For each segment: fetch flight/seat data from flight-service, then reserve seat.
     * Tracks reserved seat IDs so the caller can compensate on partial failure.
     */
    private List<SegmentData> buildAndReserveSegments(
            List<CreateBookingRequest.SegmentRequest> requests,
            List<UUID> reservedSeatIds) {

        List<SegmentData> result = new ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {
            CreateBookingRequest.SegmentRequest req = requests.get(i);
            int hopOrder = i + 1;

            // Fetch flight details for snapshot
            FlightSummary flight = flightClient.getFlight(req.flightId());

            // Verify seat belongs to the flight and is available
            SeatSummary seat = flightClient.getSeat(req.seatId());
            if (seat.isBooked()) {
                throw new SeatReservationException(
                        "Seat " + seat.seatNumber() + " on " + flight.flightNumber() + " is already booked");
            }

            // Reserve the seat
            try {
                flightClient.reserveSeat(req.seatId());
                reservedSeatIds.add(req.seatId());
                log.debug("Reserved seat {} on flight {} (hop {})",
                        seat.seatNumber(), flight.flightNumber(), hopOrder);
            } catch (feign.FeignException.Conflict e) {
                throw new SeatReservationException(
                        "Seat " + seat.seatNumber() + " was just taken. Please select another seat.");
            }

            result.add(new SegmentData(hopOrder, flight, seat));
        }
        return result;
    }

    /** Persist booking + segments + payment record in a single transaction. */
    @Transactional
    protected Booking persistBooking(CreateBookingRequest req, UUID userId,
                                     List<SegmentData> segmentDataList) {

        BigDecimal totalAmount = segmentDataList.stream()
                .map(sd -> sd.seat().price())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Booking booking = Booking.builder()
                .userId(userId)
                .bookingRef(generateUniqueBookingRef())
                .passengerFirstName(req.passengerFirstName())
                .passengerLastName(req.passengerLastName())
                .passengerEmail(req.passengerEmail())
                .passengerPhone(req.passengerPhone())
                .totalAmount(totalAmount)
                .status(BookingStatus.CONFIRMED)   // auto-confirm; integrate payment gateway for PENDING flow
                .build();

        for (SegmentData sd : segmentDataList) {
            FlightSummary flight = sd.flight();
            SeatSummary   seat   = sd.seat();

            BookingSegment segment = BookingSegment.builder()
                    .hopOrder(sd.hopOrder())
                    .flightId(flight.id())
                    .flightNumber(flight.flightNumber())
                    .airlineCode(flight.airlineCode())
                    .airlineName(flight.airlineName())
                    .originCode(flight.departureAirport().iataCode())
                    .originCity(flight.departureAirport().city())
                    .destinationCode(flight.arrivalAirport().iataCode())
                    .destinationCity(flight.arrivalAirport().city())
                    .departureTime(flight.departureTime())
                    .arrivalTime(flight.arrivalTime())
                    .seatId(seat.id())
                    .seatNumber(seat.seatNumber())
                    .seatClass(SeatClass.valueOf(seat.seatClass()))
                    .price(seat.price())
                    .build();

            booking.addSegment(segment);
        }

        // Create payment record (PENDING until gateway confirms)
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(totalAmount)
                .status(PaymentStatus.PENDING)
                .build();
        booking.setPayment(payment);

        return bookingRepo.save(booking);
    }

    /** Release all reserved seats — called on saga compensation. */
    private void compensate(List<UUID> reservedSeatIds) {
        if (reservedSeatIds.isEmpty()) return;
        log.warn("Compensating — releasing {} reserved seat(s)", reservedSeatIds.size());
        reservedSeatIds.forEach(seatId -> {
            try {
                flightClient.releaseSeat(seatId);
            } catch (Exception ex) {
                log.error("Compensation failed for seat {} — manual cleanup needed", seatId, ex);
            }
        });
    }

    private void verifyOwnership(Booking booking, UUID userId) {
        if (!booking.getUserId().equals(userId)) {
            throw new BookingNotFoundException(booking.getId());  // 404 rather than 403 to not leak existence
        }
    }

    /**
     * Generates a unique 6-character alphanumeric booking reference.
     * Retries on collision (collision probability ≈ 1 in 2.2 billion).
     */
    private String generateUniqueBookingRef() {
        String ref;
        int attempts = 0;
        do {
            if (++attempts > 10) throw new IllegalStateException("Could not generate unique booking ref");
            StringBuilder sb = new StringBuilder(BOOKING_REF_LENGTH);
            for (int i = 0; i < BOOKING_REF_LENGTH; i++) {
                sb.append(BOOKING_REF_CHARS.charAt(secureRandom.nextInt(BOOKING_REF_CHARS.length())));
            }
            ref = sb.toString();
        } while (bookingRepo.existsByBookingRef(ref));
        return ref;
    }

    /** Internal record to carry flight + seat data between steps. */
    private record SegmentData(int hopOrder, FlightSummary flight, SeatSummary seat) {}
}