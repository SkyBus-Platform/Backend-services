package com.skybus.booking.entity;

import com.skybus.booking.enums.PaymentMethod;
import com.skybus.booking.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment record linked to exactly one booking.
 * Records the outcome of an external payment gateway call.
 * This service does NOT process cards — it records gateway callbacks.
 */
@Entity
@Table(
        name   = "skybus_payment",
        schema = "skybus_booking_schema",
        indexes = {
                @Index(name = "idx_payment_booking",    columnList = "booking_id",    unique = true),
                @Index(name = "idx_payment_status",     columnList = "status"),
                @Index(name = "idx_payment_txn_id",     columnList = "transaction_id", unique = true),
                @Index(name = "idx_payment_created_at", columnList = "created_at")
        }
)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true, updatable = false)
    private Booking booking;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    @Builder.Default
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    /** External gateway transaction ID (e.g. Stripe ch_xxx). Nullable until paid. */
    @Column(name = "transaction_id", unique = true, length = 255)
    private String transactionId;

    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    // ── Helpers ───────────────────────────────────────────────────────────────
    public void markCompleted(String transactionId, String gatewayResponse) {
        this.status = PaymentStatus.COMPLETED;
        this.transactionId = transactionId;
        this.gatewayResponse = gatewayResponse;
        this.paidAt = LocalDateTime.now();
    }

    public void markRefunded() {
        this.status = PaymentStatus.REFUNDED;
        this.refundedAt = LocalDateTime.now();
    }

    public void markFailed(String gatewayResponse) {
        this.status = PaymentStatus.FAILED;
        this.gatewayResponse = gatewayResponse;
    }
}