package com.skybus.notification.consumer;

import com.skybus.notification.config.RabbitConfig;
import com.skybus.notification.dto.BookingCancelledEvent;
import com.skybus.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingCancelledConsumer {

    private final EmailService emailService;

    @RabbitListener(
            queues = RabbitConfig.QUEUE_BOOKING_CANCELLED,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void onBookingCancelled(BookingCancelledEvent event) {
        log.info("Received booking.cancelled event — ref: {}, passenger: {}",
                event.bookingRef(), event.passengerEmail());

        if (event.passengerEmail() == null || event.passengerEmail().isBlank()) {
            log.warn("booking.cancelled event has no passenger email — skipping. ref: {}",
                    event.bookingRef());
            return;
        }

        emailService.sendBookingCancellation(event);
    }
}