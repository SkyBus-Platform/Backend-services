-- ============================================================
-- V3__seed_test_flights.sql

-- ── Step 1: Insert all 1,550 flights ─────────────────────────
WITH routes(
            flight_num, airline, origin, dest,
            dep_h, dep_m, dur_h, dur_m,
            price, aircraft
    ) AS (VALUES
              -- ── CMB ↔ DXB  (3 daily each way, A320, ~3 h) ────────────
              ('UL101','UL','CMB','DXB', 6, 0,3, 0, 250.00::NUMERIC,'Airbus A320'),
              ('EK301','EK','CMB','DXB',14, 0,3, 0, 265.00::NUMERIC,'Airbus A320'),
              ('UL103','UL','CMB','DXB',22, 0,3, 0, 245.00::NUMERIC,'Airbus A320'),
              ('EK302','EK','DXB','CMB', 2, 0,3, 0, 260.00::NUMERIC,'Airbus A320'),
              ('UL102','UL','DXB','CMB',10, 0,3, 0, 255.00::NUMERIC,'Airbus A320'),
              ('EK304','EK','DXB','CMB',18, 0,3, 0, 270.00::NUMERIC,'Airbus A320'),

              -- ── CMB ↔ DOH  (2 daily each way, A320, ~3.5 h) ──────────
              ('UL201','UL','CMB','DOH', 7, 0,3,30, 280.00::NUMERIC,'Airbus A320'),
              ('QR401','QR','CMB','DOH',20, 0,3,30, 290.00::NUMERIC,'Airbus A320'),
              ('QR402','QR','DOH','CMB', 1, 0,3,30, 285.00::NUMERIC,'Airbus A320'),
              ('UL202','UL','DOH','CMB',14, 0,3,30, 275.00::NUMERIC,'Airbus A320'),

              -- ── CMB ↔ SIN  (2 daily each way, A330, ~5 h) ────────────
              ('SQ501','SQ','CMB','SIN', 8, 0,5, 0, 350.00::NUMERIC,'Airbus A330'),
              ('SQ503','SQ','CMB','SIN',23, 0,5, 0, 365.00::NUMERIC,'Airbus A330'),
              ('SQ502','SQ','SIN','CMB', 2, 0,5, 0, 355.00::NUMERIC,'Airbus A330'),
              ('SQ504','SQ','SIN','CMB',16, 0,5, 0, 360.00::NUMERIC,'Airbus A330'),

              -- ── DXB ↔ LHR  (3 daily each way, B777, ~7 h) ────────────
              ('EK001','EK','DXB','LHR', 7, 0,7, 0, 450.00::NUMERIC,'Boeing 777-300'),
              ('EK003','EK','DXB','LHR',14, 0,7, 0, 480.00::NUMERIC,'Boeing 777-300'),
              ('EK005','EK','DXB','LHR',21, 0,7, 0, 460.00::NUMERIC,'Boeing 777-300'),
              ('EK002','EK','LHR','DXB', 7,30,7, 0, 460.00::NUMERIC,'Boeing 777-300'),
              ('EK004','EK','LHR','DXB',14, 0,7, 0, 490.00::NUMERIC,'Boeing 777-300'),
              ('EK006','EK','LHR','DXB',21,30,7, 0, 470.00::NUMERIC,'Boeing 777-300'),

              -- ── DXB ↔ CDG  (2 daily each way, A330, ~7 h) ────────────
              ('EK071','EK','DXB','CDG', 9, 0,7, 0, 420.00::NUMERIC,'Airbus A330'),
              ('EK073','EK','DXB','CDG',18, 0,7, 0, 440.00::NUMERIC,'Airbus A330'),
              ('EK072','EK','CDG','DXB', 8, 0,7, 0, 430.00::NUMERIC,'Airbus A330'),
              ('EK074','EK','CDG','DXB',17, 0,7, 0, 450.00::NUMERIC,'Airbus A330'),

              -- ── DXB ↔ DOH  (4 daily each way, A320, ~1 h — Gulf hub hop)
              ('EK861','EK','DXB','DOH', 6, 0,1, 0, 150.00::NUMERIC,'Airbus A320'),
              ('QR101','QR','DXB','DOH',10, 0,1, 0, 155.00::NUMERIC,'Airbus A320'),
              ('EK863','EK','DXB','DOH',15, 0,1, 0, 150.00::NUMERIC,'Airbus A320'),
              ('QR103','QR','DXB','DOH',20, 0,1, 0, 160.00::NUMERIC,'Airbus A320'),
              ('QR102','QR','DOH','DXB', 7, 0,1, 0, 155.00::NUMERIC,'Airbus A320'),
              ('EK862','EK','DOH','DXB',11, 0,1, 0, 150.00::NUMERIC,'Airbus A320'),
              ('QR104','QR','DOH','DXB',16, 0,1, 0, 155.00::NUMERIC,'Airbus A320'),
              ('EK864','EK','DOH','DXB',21, 0,1, 0, 160.00::NUMERIC,'Airbus A320'),

              -- ── DOH ↔ LHR  (2 daily each way, B777, ~7 h) ────────────
              ('QR001','QR','DOH','LHR', 8, 0,7, 0, 430.00::NUMERIC,'Boeing 777-300'),
              ('QR003','QR','DOH','LHR',20, 0,7, 0, 450.00::NUMERIC,'Boeing 777-300'),
              ('QR002','QR','LHR','DOH', 7, 0,7, 0, 440.00::NUMERIC,'Boeing 777-300'),
              ('QR004','QR','LHR','DOH',19, 0,7, 0, 460.00::NUMERIC,'Boeing 777-300'),

              -- ── DOH ↔ CDG  (2 daily each way, A330, ~6.5 h) ──────────
              ('QR051','QR','DOH','CDG',10, 0,6,30, 400.00::NUMERIC,'Airbus A330'),
              ('QR053','QR','DOH','CDG',22, 0,6,30, 420.00::NUMERIC,'Airbus A330'),
              ('QR052','QR','CDG','DOH', 8, 0,6,30, 410.00::NUMERIC,'Airbus A330'),
              ('QR054','QR','CDG','DOH',20, 0,6,30, 430.00::NUMERIC,'Airbus A330'),

              -- ── SIN ↔ LHR  (1 daily each way, B777, ~13 h long haul) ─
              ('SQ001','SQ','SIN','LHR',23,30,13, 0, 600.00::NUMERIC,'Boeing 777-300'),
              ('SQ002','SQ','LHR','SIN',22, 0,13, 0, 620.00::NUMERIC,'Boeing 777-300'),

              -- ── LHR ↔ CDG  (4 daily each way, A320, ~1.5 h — Europe hop)
              ('EK011','EK','LHR','CDG', 7, 0,1,30, 150.00::NUMERIC,'Airbus A320'),
              ('EK013','EK','LHR','CDG',10, 0,1,30, 155.00::NUMERIC,'Airbus A320'),
              ('UL701','UL','LHR','CDG',15, 0,1,30, 150.00::NUMERIC,'Airbus A320'),
              ('UL703','UL','LHR','CDG',19, 0,1,30, 155.00::NUMERIC,'Airbus A320'),
              ('EK012','EK','CDG','LHR', 8, 0,1,30, 155.00::NUMERIC,'Airbus A320'),
              ('EK014','EK','CDG','LHR',11, 0,1,30, 155.00::NUMERIC,'Airbus A320'),
              ('UL702','UL','CDG','LHR',16, 0,1,30, 150.00::NUMERIC,'Airbus A320'),
              ('UL704','UL','CDG','LHR',20, 0,1,30, 155.00::NUMERIC,'Airbus A320')
),
     days AS (
         SELECT generate_series(0, 30) AS d   -- July 1–31 2026
     )
INSERT INTO skybus_flight_schema.skybus_flight (
    id, flight_number, airline_id, aircraft_id,
    departure_airport_id, arrival_airport_id,
    departure_time, arrival_time,
    base_price, status, available_seats
)
SELECT
    gen_random_uuid(),
    r.flight_num,
    al.id,
    ac.id,
    dep.id,
    arr.id,
    TIMESTAMP '2026-07-01 00:00:00'
        + (d.d  * INTERVAL '1 day')
        + (r.dep_h * INTERVAL '1 hour')
        + (r.dep_m * INTERVAL '1 minute'),
    TIMESTAMP '2026-07-01 00:00:00'
        + (d.d  * INTERVAL '1 day')
        + (r.dep_h * INTERVAL '1 hour')
        + (r.dep_m * INTERVAL '1 minute')
        + (r.dur_h * INTERVAL '1 hour')
        + (r.dur_m * INTERVAL '1 minute'),
    r.price,
    'SCHEDULED',
    ac.economy_seats + ac.business_seats + ac.first_class_seats
FROM routes r
         CROSS JOIN days d
         JOIN skybus_flight_schema.skybus_airline  al  ON al.iata_code  = r.airline
         JOIN skybus_flight_schema.skybus_aircraft ac  ON ac.model      = r.aircraft
         JOIN skybus_flight_schema.skybus_airport  dep ON dep.iata_code = r.origin
         JOIN skybus_flight_schema.skybus_airport  arr ON arr.iata_code = r.dest;


-- ── Step 2: Generate seats for all July 2026 flights ─────────


-- Airbus A320 — 162 seats
-- Rows 1–2:  BUSINESS (12 seats)  price × 2.0
-- Rows 3–27: ECONOMY  (150 seats) base price
INSERT INTO skybus_flight_schema.skybus_seat
(id, flight_id, seat_number, seat_class, price, is_booked, version)
SELECT
    gen_random_uuid(),
    f.id,
    r::TEXT || c,
    CASE WHEN r <= 2 THEN 'BUSINESS' ELSE 'ECONOMY' END,
    CASE WHEN r <= 2 THEN f.base_price * 2 ELSE f.base_price END,
    FALSE, 0
FROM skybus_flight_schema.skybus_flight f
         JOIN skybus_flight_schema.skybus_aircraft ac ON f.aircraft_id = ac.id
         CROSS JOIN generate_series(1, 27)                         AS r
         CROSS JOIN unnest(ARRAY['A','B','C','D','E','F'])         AS c
WHERE ac.model = 'Airbus A320'
  AND f.departure_time >= TIMESTAMP '2026-07-01 00:00:00'
  AND f.departure_time <  TIMESTAMP '2026-08-01 00:00:00';


-- Airbus A330 — 258 seats
-- Rows 1–5:  BUSINESS (30 seats)  price × 2.0
-- Rows 6–43: ECONOMY  (228 seats) base price
INSERT INTO skybus_flight_schema.skybus_seat
(id, flight_id, seat_number, seat_class, price, is_booked, version)
SELECT
    gen_random_uuid(),
    f.id,
    r::TEXT || c,
    CASE WHEN r <= 5 THEN 'BUSINESS' ELSE 'ECONOMY' END,
    CASE WHEN r <= 5 THEN f.base_price * 2 ELSE f.base_price END,
    FALSE, 0
FROM skybus_flight_schema.skybus_flight f
         JOIN skybus_flight_schema.skybus_aircraft ac ON f.aircraft_id = ac.id
         CROSS JOIN generate_series(1, 43)                         AS r
         CROSS JOIN unnest(ARRAY['A','B','C','D','E','F'])         AS c
WHERE ac.model = 'Airbus A330'
  AND f.departure_time >= TIMESTAMP '2026-07-01 00:00:00'
  AND f.departure_time <  TIMESTAMP '2026-08-01 00:00:00';


-- Boeing 777-300 — 354 seats
-- Row 1:     FIRST    (8 seats A–H) price × 3.5
-- Rows 2–8:  BUSINESS (42 seats)    price × 2.0
-- Rows 9–67: ECONOMY  (354 seats)   base price

INSERT INTO skybus_flight_schema.skybus_seat
(id, flight_id, seat_number, seat_class, price, is_booked, version)
SELECT gen_random_uuid(), f.id, '1' || c, 'FIRST', f.base_price * 3.5, FALSE, 0
FROM skybus_flight_schema.skybus_flight f
         JOIN skybus_flight_schema.skybus_aircraft ac ON f.aircraft_id = ac.id
         CROSS JOIN unnest(ARRAY['A','B','C','D','E','F','G','H']) AS c
WHERE ac.model = 'Boeing 777-300'
  AND f.departure_time >= TIMESTAMP '2026-07-01 00:00:00'
  AND f.departure_time <  TIMESTAMP '2026-08-01 00:00:00';

INSERT INTO skybus_flight_schema.skybus_seat
(id, flight_id, seat_number, seat_class, price, is_booked, version)
SELECT gen_random_uuid(), f.id, r::TEXT || c, 'BUSINESS', f.base_price * 2, FALSE, 0
FROM skybus_flight_schema.skybus_flight f
         JOIN skybus_flight_schema.skybus_aircraft ac ON f.aircraft_id = ac.id
         CROSS JOIN generate_series(2, 8)                          AS r
         CROSS JOIN unnest(ARRAY['A','B','C','D','E','F'])         AS c
WHERE ac.model = 'Boeing 777-300'
  AND f.departure_time >= TIMESTAMP '2026-07-01 00:00:00'
  AND f.departure_time <  TIMESTAMP '2026-08-01 00:00:00';

INSERT INTO skybus_flight_schema.skybus_seat
(id, flight_id, seat_number, seat_class, price, is_booked, version)
SELECT gen_random_uuid(), f.id, r::TEXT || c, 'ECONOMY', f.base_price, FALSE, 0
FROM skybus_flight_schema.skybus_flight f
         JOIN skybus_flight_schema.skybus_aircraft ac ON f.aircraft_id = ac.id
         CROSS JOIN generate_series(9, 67)                         AS r
         CROSS JOIN unnest(ARRAY['A','B','C','D','E','F'])         AS c
WHERE ac.model = 'Boeing 777-300'
  AND f.departure_time >= TIMESTAMP '2026-07-01 00:00:00'
  AND f.departure_time <  TIMESTAMP '2026-08-01 00:00:00';