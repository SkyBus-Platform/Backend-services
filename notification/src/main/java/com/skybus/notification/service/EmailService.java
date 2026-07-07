package com.skybus.notification.service;

import com.skybus.notification.dto.BookingCancelledEvent;
import com.skybus.notification.dto.BookingCreatedEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${notification.mail.from}")
    private String fromAddress;

    @Value("${notification.mail.from-name}")
    private String fromName;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH);

    // Booking confirmation
    public void sendBookingConfirmation(BookingCreatedEvent event) {
        log.info("Sending booking confirmation to {} for ref {}",
                event.passengerEmail(), event.bookingRef());

        Context ctx = new Context(Locale.ENGLISH);
        ctx.setVariable("passengerName",  event.passengerName());
        ctx.setVariable("bookingRef",     event.bookingRef());
        ctx.setVariable("totalAmount",    formatAmount(event.totalAmount(), event.currency()));
        ctx.setVariable("createdAt",      event.createdAt().format(DATE_FMT));
        ctx.setVariable("segments",       event.segments());
        ctx.setVariable("dateFmt",        DATE_FMT);

        String html = templateEngine.process("email/booking-confirmation", ctx);

        sendHtmlEmail(
                event.passengerEmail(),
                "✈ Your SkyBus booking is confirmed — " + event.bookingRef(),
                html
        );

        log.info("Confirmation email sent for booking ref {}", event.bookingRef());
    }

    // Booking cancellation
    public void sendBookingCancellation(BookingCancelledEvent event) {
        log.info("Sending cancellation notice to {} for ref {}",
                event.passengerEmail(), event.bookingRef());

        Context ctx = new Context(Locale.ENGLISH);
        ctx.setVariable("passengerName", event.passengerName());
        ctx.setVariable("bookingRef",    event.bookingRef());
        ctx.setVariable("totalAmount",   formatAmount(event.totalAmount(), "USD"));
        ctx.setVariable("cancelledAt",   event.cancelledAt().format(DATE_FMT));

        String html = templateEngine.process("email/booking-cancellation", ctx);

        sendHtmlEmail(
                event.passengerEmail(),
                "Your SkyBus booking " + event.bookingRef() + " has been cancelled",
                html
        );

        log.info("Cancellation email sent for booking ref {}", event.bookingRef());
    }

    // Internal
    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML

            mailSender.send(message);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            // Re-throw so the retry interceptor can retry the message
            throw new RuntimeException("Failed to send email to " + to + ": " + e.getMessage(), e);
        }
    }

    private String formatAmount(BigDecimal amount, String currency) {
        return currency + " " + String.format("%.2f", amount);
    }
}