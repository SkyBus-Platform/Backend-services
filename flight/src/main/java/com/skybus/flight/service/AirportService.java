package com.skybus.flight.service;

import com.skybus.flight.dto.request.CreateAirportRequest;
import com.skybus.flight.dto.response.AirportResponse;
import com.skybus.flight.entity.Airport;
import com.skybus.flight.exception.AirportNotFoundException;
import com.skybus.flight.repository.AirportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AirportService {

    private final AirportRepository airportRepo;

    @Transactional(readOnly = true)
    public List<AirportResponse> getAll() {
        return airportRepo.findAll().stream().map(AirportResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AirportResponse getByIata(String iataCode) {
        return AirportResponse.from(findByIata(iataCode));
    }

    @Transactional(readOnly = true)
    public List<AirportResponse> search(String query) {
        return airportRepo
                .findByCityContainingIgnoreCaseOrNameContainingIgnoreCase(query, query)
                .stream().map(AirportResponse::from).toList();
    }

    @Transactional
    public AirportResponse create(CreateAirportRequest req) {
        Airport airport = Airport.builder()
                .iataCode(req.iataCode().toUpperCase())
                .icaoCode(req.icaoCode())
                .name(req.name())
                .city(req.city())
                .country(req.country())
                .timezone(req.timezone())
                .latitude(req.latitude())
                .longitude(req.longitude())
                .build();
        return AirportResponse.from(airportRepo.save(airport));
    }

    // package-visible for use by FlightService
    Airport findByIata(String iataCode) {
        return airportRepo.findByIataCode(iataCode.toUpperCase())
                .orElseThrow(() -> new AirportNotFoundException(iataCode));
    }
}