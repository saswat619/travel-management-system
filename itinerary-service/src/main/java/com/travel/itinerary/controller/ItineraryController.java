package com.travel.itinerary.controller;

import com.travel.itinerary.dto.*;
import com.travel.itinerary.service.ItineraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/itinerary/itineraries")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Itinerary Management", description = "APIs for unified itinerary view, trip modifications, and booking links")
@SecurityRequirement(name = "bearerAuth")
public class ItineraryController {

    private final ItineraryService itineraryService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all itineraries - Admin only")
    public ResponseEntity<Page<ItineraryDto>> getAllItineraries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(itineraryService.getAllItineraries(PageRequest.of(page, size)));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get itineraries by user ID (CustomerID)")
    public ResponseEntity<Page<ItineraryDto>> getItinerariesByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(itineraryService.getItinerariesByUser(userId, PageRequest.of(page, size)));
    }

    @GetMapping("/user/{userId}/status/{status}")
    @Operation(summary = "Get user itineraries filtered by status")
    public ResponseEntity<Page<ItineraryDto>> getItinerariesByUserAndStatus(
            @PathVariable Long userId,
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(itineraryService.getItinerariesByUserAndStatus(userId, status, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get itinerary by ID with all bookings/items")
    public ResponseEntity<ItineraryDto> getItineraryById(@PathVariable Long id) {
        return ResponseEntity.ok(itineraryService.getItineraryById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Search itineraries by destination")
    public ResponseEntity<Page<ItineraryDto>> searchByDestination(
            @RequestParam String destination,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(itineraryService.searchByDestination(destination, PageRequest.of(page, size)));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming itineraries")
    public ResponseEntity<Page<ItineraryDto>> getUpcomingItineraries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(itineraryService.getUpcomingItineraries(PageRequest.of(page, size)));
    }

    @PostMapping
    @Operation(summary = "Create a new itinerary")
    public ResponseEntity<ItineraryDto> createItinerary(@Valid @RequestBody ItineraryRequest req) {
        log.info("Creating itinerary for user: {}", req.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(itineraryService.createItinerary(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update itinerary details")
    public ResponseEntity<ItineraryDto> updateItinerary(@PathVariable Long id, @Valid @RequestBody ItineraryRequest req) {
        return ResponseEntity.ok(itineraryService.updateItinerary(id, req));
    }

    // === BOOKING LINKS (Bookings[] from Travel360 spec) ===

    @GetMapping("/{id}/bookings")
    @Operation(summary = "Get all bookings linked to this itinerary")
    public ResponseEntity<List<ItineraryBookingDto>> getItineraryBookings(@PathVariable Long id) {
        return ResponseEntity.ok(itineraryService.getItineraryBookings(id));
    }

    @PostMapping("/{id}/bookings")
    @Operation(summary = "Link a booking to this itinerary (adds to Bookings[])")
    public ResponseEntity<ItineraryDto> linkBooking(
            @PathVariable Long id, @Valid @RequestBody LinkBookingRequest req) {
        log.info("Linking booking {} to itinerary {}", req.getBookingId(), id);
        return ResponseEntity.ok(itineraryService.linkBooking(id, req));
    }

    @DeleteMapping("/{id}/bookings/{bookingId}")
    @Operation(summary = "Unlink a booking from this itinerary")
    public ResponseEntity<ItineraryDto> unlinkBooking(@PathVariable Long id, @PathVariable Long bookingId) {
        return ResponseEntity.ok(itineraryService.unlinkBooking(id, bookingId));
    }

    // === ITEM MANAGEMENT ===

    @PostMapping("/{id}/items")
    @Operation(summary = "Add activity item to itinerary day")
    public ResponseEntity<ItineraryDto> addItem(@PathVariable Long id, @Valid @RequestBody ItineraryItemRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itineraryService.addItemToItinerary(id, req));
    }

    @PutMapping("/{id}/items/{itemId}")
    @Operation(summary = "Update an itinerary activity item")
    public ResponseEntity<ItineraryDto> updateItem(@PathVariable Long id, @PathVariable Long itemId,
                                                    @Valid @RequestBody ItineraryItemRequest req) {
        return ResponseEntity.ok(itineraryService.updateItineraryItem(id, itemId, req));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @Operation(summary = "Remove an activity item from itinerary")
    public ResponseEntity<Void> removeItem(@PathVariable Long id, @PathVariable Long itemId) {
        itineraryService.removeItemFromItinerary(id, itemId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/items/{itemId}/complete")
    @Operation(summary = "Mark an activity as completed or pending")
    public ResponseEntity<ItineraryDto> markItemCompleted(@PathVariable Long id, @PathVariable Long itemId,
                                                           @RequestParam boolean completed) {
        return ResponseEntity.ok(itineraryService.markItemCompleted(id, itemId, completed));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TRAVELER','TRAVEL_AGENT')")
    @Operation(summary = "Delete an itinerary")
    public ResponseEntity<Void> deleteItinerary(@PathVariable Long id) {
        itineraryService.deleteItinerary(id);
        return ResponseEntity.noContent().build();
    }
}
