-- V1__create_booking_schema.sql

CREATE SCHEMA IF NOT EXISTS skybus_booking_schema;

-- ── Bookings ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS skybus_booking_schema.skybus_booking (
    id                   UUID           NOT NULL DEFAULT gen_random_uuid(),
    user_id              UUID           NOT NULL,
    booking_ref          VARCHAR(6)        NOT NULL,
    passenger_first_name VARCHAR(100)   NOT NULL,
    passenger_last_name  VARCHAR(100)   NOT NULL,
    passenger_email      VARCHAR(255)   NOT NULL,
    passenger_phone      VARCHAR(20),
    total_amount         DECIMAL(10,2)  NOT NULL CHECK (total_amount >= 0),
    currency             VARCHAR(3)        NOT NULL DEFAULT 'USD',
    status               VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    created_at           TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP      NOT NULL DEFAULT NOW(),
    cancelled_at         TIMESTAMP,

    CONSTRAINT pk_booking         PRIMARY KEY (id),
    CONSTRAINT uq_booking_ref     UNIQUE (booking_ref),
    CONSTRAINT chk_booking_status CHECK (status IN ('PENDING','CONFIRMED','CANCELLED','COMPLETED'))
    );

CREATE INDEX idx_booking_user_id     ON skybus_booking_schema.skybus_booking (user_id);
CREATE INDEX idx_booking_ref         ON skybus_booking_schema.skybus_booking (booking_ref);
CREATE INDEX idx_booking_status      ON skybus_booking_schema.skybus_booking (status);
CREATE INDEX idx_booking_user_status ON skybus_booking_schema.skybus_booking (user_id, status);
CREATE INDEX idx_booking_created_at  ON skybus_booking_schema.skybus_booking (created_at);

-- Auto-update updated_at
CREATE OR REPLACE FUNCTION skybus_booking_schema.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_booking_updated_at
    BEFORE UPDATE ON skybus_booking_schema.skybus_booking
    FOR EACH ROW EXECUTE FUNCTION skybus_booking_schema.set_updated_at();

-- ── Booking Segments ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS skybus_booking_schema.skybus_booking_segment (
    id               UUID           NOT NULL DEFAULT gen_random_uuid(),
    booking_id       UUID           NOT NULL,
    hop_order        INT            NOT NULL CHECK (hop_order >= 1),

    -- Flight snapshot (no FK to flight-service)
    flight_id        UUID           NOT NULL,
    flight_number    VARCHAR(10)    NOT NULL,
    airline_code     VARCHAR(3),
    airline_name     VARCHAR(150),

    -- Route snapshot
    origin_code      VARCHAR(3)        NOT NULL,
    origin_city      VARCHAR(100),
    destination_code VARCHAR(3)        NOT NULL,
    destination_city VARCHAR(100),
    departure_time   TIMESTAMP      NOT NULL,
    arrival_time     TIMESTAMP      NOT NULL,

    -- Seat snapshot (no FK to flight-service)
    seat_id          UUID           NOT NULL,
    seat_number      VARCHAR(4)     NOT NULL,
    seat_class       VARCHAR(20)    NOT NULL,
    price            DECIMAL(10,2)  NOT NULL CHECK (price >= 0),

    CONSTRAINT pk_booking_segment   PRIMARY KEY (id),
    CONSTRAINT fk_segment_booking   FOREIGN KEY (booking_id)
    REFERENCES skybus_booking_schema.skybus_booking (id) ON DELETE CASCADE,
    CONSTRAINT chk_segment_times    CHECK (arrival_time > departure_time),
    CONSTRAINT chk_seat_class       CHECK (seat_class IN ('ECONOMY','BUSINESS','FIRST'))
    );

CREATE INDEX idx_seg_booking_id ON skybus_booking_schema.skybus_booking_segment (booking_id);
CREATE INDEX idx_seg_flight_id  ON skybus_booking_schema.skybus_booking_segment (flight_id);
CREATE INDEX idx_seg_seat_id    ON skybus_booking_schema.skybus_booking_segment (seat_id);
CREATE INDEX idx_seg_origin     ON skybus_booking_schema.skybus_booking_segment (origin_code);
CREATE INDEX idx_seg_dest       ON skybus_booking_schema.skybus_booking_segment (destination_code);
CREATE INDEX idx_seg_dep_time   ON skybus_booking_schema.skybus_booking_segment (departure_time);

-- ── Payments ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS skybus_booking_schema.skybus_payment (
    id               UUID           NOT NULL DEFAULT gen_random_uuid(),
    booking_id       UUID           NOT NULL,
    amount           DECIMAL(10,2)  NOT NULL CHECK (amount >= 0),
    currency         VARCHAR(3)        NOT NULL DEFAULT 'USD',
    payment_method   VARCHAR(30),
    status           VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    transaction_id   VARCHAR(255),
    gateway_response TEXT,
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    paid_at          TIMESTAMP,
    refunded_at      TIMESTAMP,

    CONSTRAINT pk_payment          PRIMARY KEY (id),
    CONSTRAINT uq_payment_booking  UNIQUE (booking_id),
    CONSTRAINT uq_payment_txn_id   UNIQUE (transaction_id),
    CONSTRAINT fk_payment_booking  FOREIGN KEY (booking_id)
    REFERENCES skybus_booking_schema.skybus_booking (id) ON DELETE CASCADE,
    CONSTRAINT chk_payment_status  CHECK (status IN ('PENDING','PROCESSING','COMPLETED','FAILED','REFUNDED')),
    CONSTRAINT chk_payment_method  CHECK (
        payment_method IN ('CREDIT_CARD','DEBIT_CARD','PAYPAL','BANK_TRANSFER')
        OR payment_method IS NULL
    )
    );

CREATE INDEX idx_payment_booking    ON skybus_booking_schema.skybus_payment (booking_id);
CREATE INDEX idx_payment_status     ON skybus_booking_schema.skybus_payment (status);
CREATE INDEX idx_payment_created_at ON skybus_booking_schema.skybus_payment (created_at);

COMMENT ON TABLE skybus_booking_schema.skybus_booking         IS 'Booking aggregate root — owned by booking-service. No FKs to other schemas.';
COMMENT ON TABLE skybus_booking_schema.skybus_booking_segment IS 'Full snapshot of flight + seat at booking time. Survives flight data changes.';
COMMENT ON TABLE skybus_booking_schema.skybus_payment         IS 'Payment record — records external gateway outcome.';