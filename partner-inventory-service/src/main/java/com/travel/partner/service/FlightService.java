package com.travel.partner.service;

import com.travel.partner.dto.FlightDto;
import com.travel.partner.dto.FlightRequest;
import com.travel.partner.entity.Flight;
import com.travel.partner.entity.Partner;
import com.travel.partner.exception.ResourceNotFoundException;
import com.travel.partner.projection.FlightSummary;
import com.travel.partner.repository.FlightRepository;
import com.travel.partner.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlightService {

    private final FlightRepository flightRepository;
    private final PartnerRepository partnerRepository;

    public Page<FlightSummary> getAllFlightSummaries(Pageable pageable) {
        log.info("Fetching all flight summaries");
        return flightRepository.findAllByActiveTrueOrderByDepartureTimeAsc(pageable);
    }

    public Page<FlightDto> getAllFlights(Pageable pageable) {
        return flightRepository.findByActiveTrueOrderByDepartureTimeAsc(pageable)
                .map(this::mapToDto);
    }

    public FlightDto getFlightById(Long id) {
        log.info("Fetching flight with id: {}", id);
        return flightRepository.findByIdAndActiveTrue(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", id));
    }

    public FlightDto getFlightByNumber(String flightNumber) {
        return flightRepository.findByFlightNumber(flightNumber)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "flightNumber", flightNumber));
    }

    public Page<FlightDto> searchFlights(String origin, String destination,
                                          LocalDateTime departureDate, Integer seats,
                                          Pageable pageable) {
        log.info("Searching flights from {} to {} on {}", origin, destination, departureDate);
        return flightRepository.searchFlights(origin, destination, departureDate, seats, pageable)
                .map(this::mapToDto);
    }

    public Page<FlightDto> getFlightsByPartner(Long partnerId, Pageable pageable) {
        return flightRepository.findByPartnerId(partnerId, pageable).map(this::mapToDto);
    }

    public Page<FlightDto> getFlightsByStatus(String status, Pageable pageable) {
        return flightRepository.findByStatus(status, pageable).map(this::mapToDto);
    }

    @Transactional
    public FlightDto createFlight(FlightRequest req) {
        log.info("Creating flight: {}", req.getFlightNumber());
        Partner partner = partnerRepository.findById(req.getPartnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner", "id", req.getPartnerId()));

        Flight flight = Flight.builder()
                .flightNumber(req.getFlightNumber())
                .partner(partner)
                .origin(req.getOrigin())
                .destination(req.getDestination())
                .originAirportCode(req.getOriginAirportCode())
                .destinationAirportCode(req.getDestinationAirportCode())
                .departureTime(req.getDepartureTime())
                .arrivalTime(req.getArrivalTime())
                .durationMinutes(req.getDurationMinutes())
                .totalSeats(req.getTotalSeats())
                .availableSeats(req.getAvailableSeats())
                .priceEconomy(req.getPriceEconomy())
                .priceBusiness(req.getPriceBusiness())
                .priceFirstClass(req.getPriceFirstClass())
                .availableCabinClasses(req.getAvailableCabinClasses())
                .aircraftType(req.getAircraftType())
                .status("SCHEDULED")
                .active(true)
                .build();

        return mapToDto(flightRepository.save(flight));
    }

    @Transactional
    public FlightDto updateFlight(Long id, FlightRequest req) {
        log.info("Updating flight with id: {}", id);
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", id));

        Partner partner = partnerRepository.findById(req.getPartnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner", "id", req.getPartnerId()));

        flight.setFlightNumber(req.getFlightNumber());
        flight.setPartner(partner);
        flight.setOrigin(req.getOrigin());
        flight.setDestination(req.getDestination());
        flight.setOriginAirportCode(req.getOriginAirportCode());
        flight.setDestinationAirportCode(req.getDestinationAirportCode());
        flight.setDepartureTime(req.getDepartureTime());
        flight.setArrivalTime(req.getArrivalTime());
        flight.setDurationMinutes(req.getDurationMinutes());
        flight.setTotalSeats(req.getTotalSeats());
        flight.setAvailableSeats(req.getAvailableSeats());
        flight.setPriceEconomy(req.getPriceEconomy());
        flight.setPriceBusiness(req.getPriceBusiness());
        flight.setPriceFirstClass(req.getPriceFirstClass());
        flight.setAvailableCabinClasses(req.getAvailableCabinClasses());
        flight.setAircraftType(req.getAircraftType());

        return mapToDto(flightRepository.save(flight));
    }

    @Transactional
    public FlightDto updateFlightStatus(Long id, String status) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", id));
        flight.setStatus(status);
        return mapToDto(flightRepository.save(flight));
    }

    @Transactional
    public FlightDto updateAvailableSeats(Long id, int delta) {
        log.info("Updating available seats for flight id: {}, delta: {}", id, delta);
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", id));
        int newAvailable = flight.getAvailableSeats() + delta;
        if (newAvailable < 0) {
            throw new IllegalStateException("Insufficient seats for flight id: " + id);
        }
        flight.setAvailableSeats(newAvailable);
        return mapToDto(flightRepository.save(flight));
    }

    @Transactional
    public void deleteFlight(Long id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", id));
        flight.setActive(false);
        flightRepository.save(flight);
        log.info("Soft deleted flight with id: {}", id);
    }

    private FlightDto mapToDto(Flight f) {
        return FlightDto.builder()
                .id(f.getId())
                .flightNumber(f.getFlightNumber())
                .partnerId(f.getPartner() != null ? f.getPartner().getId() : null)
                .airlineName(f.getPartner() != null ? f.getPartner().getName() : null)
                .origin(f.getOrigin())
                .destination(f.getDestination())
                .originAirportCode(f.getOriginAirportCode())
                .destinationAirportCode(f.getDestinationAirportCode())
                .departureTime(f.getDepartureTime())
                .arrivalTime(f.getArrivalTime())
                .durationMinutes(f.getDurationMinutes())
                .totalSeats(f.getTotalSeats())
                .availableSeats(f.getAvailableSeats())
                .priceEconomy(f.getPriceEconomy())
                .priceBusiness(f.getPriceBusiness())
                .priceFirstClass(f.getPriceFirstClass())
                .availableCabinClasses(f.getAvailableCabinClasses())
                .aircraftType(f.getAircraftType())
                .status(f.getStatus())
                .active(f.isActive())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
