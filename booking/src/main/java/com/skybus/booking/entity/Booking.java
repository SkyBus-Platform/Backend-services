package com.skybus.booking.entity;

import com.skybus.booking.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate root of the booking domain.
 * All flight and passenger data is stored as a snapshot at booking time —
 * no foreign keys cross into flight-service or user-service databases.
 */
@Entity
@Table(
        name   = "skybus_booking",
        schema = "skybus_booking_schema",
        indexes = {
                @Index(name = "idx_booking_user_id",     columnList = "user_id"),
                @Index(name = "idx_booking_ref",         columnList = "booking_ref",  unique = true),
                @Index(name = "idx_booking_status",      columnList = "status"),
                @Index(name = "idx_booking_user_status", columnList = "user_id,status"),
                @Index(name = "idx_booking_created_at",  columnList = "created_at")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /** Cross-service UUID reference — not a foreign key. */
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    /** Human-readable 6-char alphanumeric code shown on boarding pass. */
    @Column(name = "booking_ref", nullable = false, unique = true, length = 6, updatable = false)
    private String bookingRef;

    // ── Passenger snapshot ────────────────────────────────────────────────────
    @Column(name = "passenger_first_name", nullable = false, length = 100)
    private String passengerFirstName;

    @Column(name = "passenger_last_name", nullable = false, length = 100)
    private String passengerLastName;

    @Column(name = "passenger_email", nullable = false, length = 255)
    private String passengerEmail;

    @Column(name = "passenger_phone", length = 20)
    private String passengerPhone;

    // ── Financials ────────────────────────────────────────────────────────────
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 3)
    @Builder.Default
    private String currency = "USD";

    // ── Status ────────────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    // ── Timestamps ────────────────────────────────────────────────────────────
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // ── Relationships ─────────────────────────────────────────────────────────
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("hopOrder ASC")
    @Builder.Default
    private List<BookingSegment> segments = new ArrayList<>();

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private Payment payment;

    // ── Helpers ───────────────────────────────────────────────────────────────
    public void addSegment(BookingSegment segment) {
        segments.add(segment);
        segment.setBooking(this);
    }

    public void cancel() {
        if (status == BookingStatus.COMPLETED)
            throw new IllegalStateException("Cannot cancel a completed booking");
        if (status == BookingStatus.CANCELLED)
            throw new IllegalStateException("Booking already cancelled");
        this.status = BookingStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    public void confirm() {
        this.status = BookingStatus.CONFIRMED;
    }

    public String getPassengerFullName() {
        return passengerFirstName + " " + passengerLastName;
    }
}