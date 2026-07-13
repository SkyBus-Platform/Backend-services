package com.skybus.notification.consumer;

import com.skybus.notification.config.RabbitConfig;
import com.skybus.notification.dto.BookingCreatedEvent;
import com.skybus.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumes booking.created events from RabbitMQ.
 *
 * The retry interceptor (configured in RabbitConfig) wraps this method:
 *   - Attempt 1 fails → wait 1s → retry
 *   - Attempt 2 fails → wait 2s → retry
 *   - Attempt 3 fails → reject → message goes to DLQ
 *
 * Idempotency note:
 *   If the same bookingRef is processed twice (e.g. due to a network glitch
 *   causing redelivery before the ack), the passenger receives a duplicate
 *   email. For production, store processed bookingRefs in Redis with a TTL
 *   and skip if already seen.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingCreatedConsumer {

    private final EmailService emailService;

    @RabbitListener(
            queues = RabbitConfig.QUEUE_BOOKING_CREATED,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void onBookingCreated(BookingCreatedEvent event) {
        log.info("Received booking.created event — ref: {}, passenger: {}",
                event.bookingRef(), event.passengerEmail());

        if (event.passengerEmail() == null || event.passengerEmail().isBlank()) {
            log.warn("booking.created event has no passenger email — skipping. ref: {}",
                    event.bookingRef());
            return;   // ack the message — nothing we can do without an email
        }

        emailService.sendBookingConfirmation(event);
    }
}