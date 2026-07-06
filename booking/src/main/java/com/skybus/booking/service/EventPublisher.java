package com.skybus.booking.service;

import com.skybus.booking.config.RabbitConfig;
import com.skybus.booking.entity.Booking;
import com.skybus.booking.event.BookingCancelledEvent;
import com.skybus.booking.event.BookingCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishBookingCreated(Booking booking) {
        try {
            List<BookingCreatedEvent.SegmentInfo> segmentInfos = booking.getSegments()
                    .stream()
                    .map(s -> new BookingCreatedEvent.SegmentInfo(
                            s.getHopOrder(), s.getFlightNumber(), s.getAirlineCode(),
                            s.getOriginCode(), s.getOriginCity(),
                            s.getDestinationCode(), s.getDestinationCity(),
                            s.getDepartureTime(), s.getArrivalTime(),
                            s.getSeatNumber(), s.getSeatClass().name(), s.getPrice()
                    ))
                    .toList();

            BookingCreatedEvent event = new BookingCreatedEvent(
                    booking.getId(), booking.getBookingRef(), booking.getUserId(),
                    booking.getPassengerEmail(), booking.getPassengerFullName(),
                    booking.getTotalAmount(), booking.getCurrency(),
                    booking.getCreatedAt(), segmentInfos
            );

            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE,
                    RabbitConfig.ROUTING_CREATED,
                    event
            );
            log.info("Published booking.created for ref {}", booking.getBookingRef());

        } catch (Exception e) {
            // Non-critical — booking is already saved. Log and move on.
            // In production, use transactional outbox pattern to guarantee delivery.
            log.error("Failed to publish booking.created for ref {}: {}",
                    booking.getBookingRef(), e.getMessage());
        }
    }

    public void publishBookingCancelled(Booking booking) {
        try {
            BookingCancelledEvent event = new BookingCancelledEvent(
                    booking.getId(), booking.getBookingRef(), booking.getUserId(),
                    booking.getPassengerEmail(), booking.getPassengerFullName(),
                    booking.getTotalAmount(), booking.getCancelledAt()
            );
            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE,
                    RabbitConfig.ROUTING_CANCELLED,
                    event
            );
            log.info("Published booking.cancelled for ref {}", booking.getBookingRef());

        } catch (Exception e) {
            log.error("Failed to publish booking.cancelled for ref {}: {}",
                    booking.getBookingRef(), e.getMessage());
        }
    }
}