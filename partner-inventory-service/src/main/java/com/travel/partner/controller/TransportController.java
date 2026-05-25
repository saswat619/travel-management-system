package com.travel.partner.controller;

import com.travel.partner.dto.TransportDto;
import com.travel.partner.dto.TransportRequest;
import com.travel.partner.projection.TransportSummary;
import com.travel.partner.service.TransportService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/partner/transports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transport Management", description = "APIs for managing transport providers: car rentals, buses, shuttles")
@SecurityRequirement(name = "bearerAuth")
public class TransportController {

    private final TransportService transportService;

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(summary = "Get all available transports with pagination")
    public ResponseEntity<Page<TransportDto>> getAvailableTransports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transportType") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(transportService.getAvailableTransports(pageable));
    }

    @GetMapping("/summaries")
    @Operation(summary = "Get transport summaries (projection)")
    public ResponseEntity<Page<TransportSummary>> getTransportSummaries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(transportService.getAllTransportSummaries(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transport by ID")
    public ResponseEntity<TransportDto> getTransportById(@PathVariable Long id) {
        return ResponseEntity.ok(transportService.getTransportById(id));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get transports by type (CAR_RENTAL/BUS/SHUTTLE/TRANSFER/TAXI)")
    public ResponseEntity<Page<TransportDto>> getTransportsByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(transportService.getTransportsByType(type, PageRequest.of(page, size)));
    }

    @GetMapping("/partner/{partnerId}")
    @Operation(summary = "Get transports by partner")
    public ResponseEntity<Page<TransportDto>> getTransportsByPartner(
            @PathVariable Long partnerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(transportService.getTransportsByPartner(partnerId, PageRequest.of(page, size)));
    }

    @GetMapping("/search/route")
    @Operation(summary = "Search transports by pickup and dropoff location")
    public ResponseEntity<Page<TransportDto>> searchByRoute(
            @RequestParam String pickup,
            @RequestParam String dropoff,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(transportService.searchByRoute(pickup, dropoff, PageRequest.of(page, size)));
    }

    @GetMapping("/search/area")
    @Operation(summary = "Search transports by coverage area")
    public ResponseEntity<Page<TransportDto>> searchByCoverageArea(
            @RequestParam String area,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(transportService.searchByCoverageArea(area, PageRequest.of(page, size)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a new transport vehicle/service")
    public ResponseEntity<TransportDto> createTransport(@Valid @RequestBody TransportRequest req) {
        log.info("Creating new transport: {}", req.getVehicleName());
        return ResponseEntity.status(HttpStatus.CREATED).body(transportService.createTransport(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update transport details")
    public ResponseEntity<TransportDto> updateTransport(@PathVariable Long id, @Valid @RequestBody TransportRequest req) {
        return ResponseEntity.ok(transportService.updateTransport(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft delete a transport")
    public ResponseEntity<Void> deleteTransport(@PathVariable Long id) {
        transportService.deleteTransport(id);
        return ResponseEntity.noContent().build();
    }
}
