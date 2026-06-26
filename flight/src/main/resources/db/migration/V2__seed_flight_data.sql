-- Sample data for local development and testing.

-- ── Airlines ─────────────────────────────────────────────────
INSERT INTO skybus_flight_schema.skybus_airline (id, iata_code, name, country) VALUES
    (gen_random_uuid(), 'UL', 'SriLankan Airlines',  'Sri Lanka'),
    (gen_random_uuid(), 'EK', 'Emirates',            'UAE'),
    (gen_random_uuid(), 'QR', 'Qatar Airways',       'Qatar'),
    (gen_random_uuid(), 'SQ', 'Singapore Airlines',  'Singapore')
    ON CONFLICT (iata_code) DO NOTHING;

-- ── Aircraft ──────────────────────────────────────────────────
INSERT INTO skybus_flight_schema.skybus_aircraft
(id, model, type_code, economy_seats, business_seats, first_class_seats) VALUES
    (gen_random_uuid(), 'Airbus A320',    'A320', 150, 12, 0),
    (gen_random_uuid(), 'Airbus A330',    'A333', 220, 30, 8),
    (gen_random_uuid(), 'Boeing 777-300', 'B773', 304, 42, 8)
    ON CONFLICT DO NOTHING;

-- ── Airports ──────────────────────────────────────────────────
INSERT INTO skybus_flight_schema.skybus_airport
(id, iata_code, icao_code, name, city, country, timezone, latitude, longitude) VALUES
    (gen_random_uuid(), 'CMB', 'VCBI', 'Bandaranaike International Airport', 'Colombo',   'Sri Lanka',   'Asia/Colombo',    7.1801,  79.8841),
    (gen_random_uuid(), 'DXB', 'OMDB', 'Dubai International Airport',        'Dubai',     'UAE',         'Asia/Dubai',     25.2532,  55.3657),
    (gen_random_uuid(), 'DOH', 'OTHH', 'Hamad International Airport',        'Doha',      'Qatar',       'Asia/Qatar',     25.2609,  51.6138),
    (gen_random_uuid(), 'SIN', 'WSSS', 'Changi Airport',                     'Singapore', 'Singapore',   'Asia/Singapore', 1.3644,  103.9915),
    (gen_random_uuid(), 'LHR', 'EGLL', 'Heathrow Airport',                   'London',    'UK',          'Europe/London',  51.4700,  -0.4543),
    (gen_random_uuid(), 'CDG', 'LFPG', 'Charles de Gaulle Airport',          'Paris',     'France',      'Europe/Paris',   49.0097,   2.5479)
    ON CONFLICT (iata_code) DO NOTHING;