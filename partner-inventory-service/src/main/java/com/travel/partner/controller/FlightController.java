package com.travel.partner.controller;

import com.travel.partner.dto.FlightDto;
import com.travel.partner.dto.FlightRequest;
import com.travel.partner.projection.FlightSummary;
import com.travel.partner.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/partner/flights")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Flight Management", description = "APIs for managing airline flights and seat inventory")
@SecurityRequirement(name = "bearerAuth")
public class FlightController {

    private final FlightService flightService;

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(summary = "Get all flights with pagination")
    public ResponseEntity<Page<FlightDto>> getAllFlights(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "departureTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(flightService.getAllFlights(pageable));
    }

    @GetMapping("/summaries")
    @Operation(summary = "Get flight summaries (projection)")
    public ResponseEntity<Page<FlightSummary>> getFlightSummaries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(flightService.getAllFlightSummaries(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get flight by ID")
    public ResponseEntity<FlightDto> getFlightById(@PathVariable Long id) {
        return ResponseEntity.ok(flightService.getFlightById(id));
    }

    @GetMapping("/number/{flightNumber}")
    @Operation(summary = "Get flight by flight number")
    public ResponseEntity<FlightDto> getFlightByNumber(@PathVariable String flightNumber) {
        return ResponseEntity.ok(flightService.getFlightByNumber(flightNumber));
    }

    @GetMapping("/search")
    @Operation(summary = "Search flights by origin, destination, date and seats")
    public ResponseEntity<Page<FlightDto>> searchFlights(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime departureDate,
            @RequestParam(defaultValue = "1") Integer seats,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(flightService.searchFlights(origin, destination, departureDate, seats, PageRequest.of(page, size)));
    }

    @GetMapping("/partner/{partnerId}")
    @Operation(summary = "Get flights by airline partner")
    public ResponseEntity<Page<FlightDto>> getFlightsByPartner(
            @PathVariable Long partnerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(flightService.getFlightsByPartner(partnerId, PageRequest.of(page, size)));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get flights by status (SCHEDULED/DELAYED/CANCELLED/COMPLETED)")
    public ResponseEntity<Page<FlightDto>> getFlightsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(flightService.getFlightsByStatus(status, PageRequest.of(page, size)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new flight")
    public ResponseEntity<FlightDto> createFlight(@Valid @RequestBody FlightRequest req) {
        log.info("Creating new flight: {}", req.getFlightNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(flightService.createFlight(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update flight details")
    public ResponseEntity<FlightDto> updateFlight(@PathVariable Long id, @Valid @RequestBody FlightRequest req) {
        return ResponseEntity.ok(flightService.updateFlight(id, req));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update flight status")
    public ResponseEntity<FlightDto> updateFlightStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(flightService.updateFlightStatus(id, status));
    }

    @PatchMapping("/{id}/seats")
    @Operation(summary = "Update available seat count (delta can be negative to reserve, positive to release)")
    public ResponseEntity<FlightDto> updateAvailableSeats(@PathVariable Long id, @RequestParam int delta) {
        log.info("Updating available seats for flight id: {}, delta: {}", id, delta);
        return ResponseEntity.ok(flightService.updateAvailableSeats(id, delta));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft delete a flight")
    public ResponseEntity<Void> deleteFlight(@PathVariable Long id) {
        flightService.deleteFlight(id);
        return ResponseEntity.noContent().build();
    }
}
