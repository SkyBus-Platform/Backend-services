CREATE SCHEMA IF NOT EXISTS skybus_flight_schema;

CREATE TABLE IF NOT EXISTS skybus_flight_schema.skybus_airline (
    id        UUID         NOT NULL DEFAULT gen_random_uuid(),
    iata_code VARCHAR(3)   NOT NULL,
    name      VARCHAR(150) NOT NULL,
    country   VARCHAR(100),
    logo_url  VARCHAR(500),
    is_active BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_airline     PRIMARY KEY (id),
    CONSTRAINT uq_airline_iata UNIQUE (iata_code)
    );

CREATE TABLE IF NOT EXISTS skybus_flight_schema.skybus_aircraft (
    id                 UUID         NOT NULL DEFAULT gen_random_uuid(),
    model              VARCHAR(100) NOT NULL,
    type_code          VARCHAR(10),
    economy_seats      INT          NOT NULL CHECK (economy_seats >= 0),
    business_seats     INT          NOT NULL DEFAULT 0,
    first_class_seats  INT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_aircraft PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS skybus_flight_schema.skybus_airport (
    id           UUID         NOT NULL DEFAULT gen_random_uuid(),
    iata_code    VARCHAR(3)      NOT NULL,
    icao_code    VARCHAR(4),
    name         VARCHAR(200) NOT NULL,
    city         VARCHAR(100) NOT NULL,
    country      VARCHAR(100) NOT NULL,
    timezone     VARCHAR(50),
    latitude     DECIMAL(9,6),
    longitude    DECIMAL(9,6),
    CONSTRAINT pk_airport      PRIMARY KEY (id),
    CONSTRAINT uq_airport_iata UNIQUE (iata_code)
    );

CREATE INDEX idx_airport_city ON skybus_flight_schema.skybus_airport (city);

CREATE TABLE IF NOT EXISTS skybus_flight_schema.skybus_flight (
    id                   UUID          NOT NULL DEFAULT gen_random_uuid(),
    flight_number        VARCHAR(10)   NOT NULL,
    airline_id           UUID          NOT NULL,
    aircraft_id          UUID          NOT NULL,
    departure_airport_id UUID          NOT NULL,
    arrival_airport_id   UUID          NOT NULL,
    departure_time       TIMESTAMP     NOT NULL,
    arrival_time         TIMESTAMP     NOT NULL,
    base_price           DECIMAL(10,2) NOT NULL CHECK (base_price > 0),
    status               VARCHAR(20)   NOT NULL DEFAULT 'SCHEDULED',
    available_seats      INT           NOT NULL DEFAULT 0,
    CONSTRAINT pk_flight              PRIMARY KEY (id),
    CONSTRAINT uq_flight_num_dep      UNIQUE (flight_number, departure_time),
    CONSTRAINT fk_flight_airline      FOREIGN KEY (airline_id)
    REFERENCES skybus_flight_schema.skybus_airline (id),
    CONSTRAINT fk_flight_aircraft     FOREIGN KEY (aircraft_id)
    REFERENCES skybus_flight_schema.skybus_aircraft (id),
    CONSTRAINT fk_flight_dep_airport  FOREIGN KEY (departure_airport_id)
    REFERENCES skybus_flight_schema.skybus_airport (id),
    CONSTRAINT fk_flight_arr_airport  FOREIGN KEY (arrival_airport_id)
    REFERENCES skybus_flight_schema.skybus_airport (id),
    CONSTRAINT chk_flight_times       CHECK (arrival_time > departure_time),
    CONSTRAINT chk_flight_status      CHECK (status IN
('SCHEDULED','DELAYED','BOARDING','DEPARTED','ARRIVED','CANCELLED'))
    );

CREATE INDEX idx_flight_dep_airport ON skybus_flight_schema.skybus_flight (departure_airport_id);
CREATE INDEX idx_flight_arr_airport ON skybus_flight_schema.skybus_flight (arrival_airport_id);
CREATE INDEX idx_flight_dep_time    ON skybus_flight_schema.skybus_flight (departure_time);
CREATE INDEX idx_flight_status      ON skybus_flight_schema.skybus_flight (status);
CREATE INDEX idx_flight_search      ON skybus_flight_schema.skybus_flight
    (departure_airport_id, departure_time, status);

CREATE TABLE IF NOT EXISTS skybus_flight_schema.skybus_seat (
    id          UUID          NOT NULL DEFAULT gen_random_uuid(),
    flight_id   UUID          NOT NULL,
    seat_number VARCHAR(4)    NOT NULL,
    seat_class  VARCHAR(20)   NOT NULL,
    price       DECIMAL(10,2) NOT NULL CHECK (price > 0),
    is_booked   BOOLEAN       NOT NULL DEFAULT FALSE,
    version     BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_seat              PRIMARY KEY (id),
    CONSTRAINT uq_seat_flight_num   UNIQUE (flight_id, seat_number),
    CONSTRAINT fk_seat_flight       FOREIGN KEY (flight_id)
    REFERENCES skybus_flight_schema.skybus_flight (id) ON DELETE CASCADE,
    CONSTRAINT chk_seat_class       CHECK (seat_class IN ('ECONOMY','BUSINESS','FIRST'))
    );

CREATE INDEX idx_seat_flight        ON skybus_flight_schema.skybus_seat (flight_id);
CREATE INDEX idx_seat_flight_booked ON skybus_flight_schema.skybus_seat (flight_id, is_booked);
CREATE INDEX idx_seat_flight_class  ON skybus_flight_schema.skybus_seat (flight_id, seat_class, is_booked);