package com.travel.booking.controller;

import com.travel.booking.dto.BookingDto;
import com.travel.booking.dto.BookingRequest;
import com.travel.booking.dto.BookingStatusUpdateRequest;
import com.travel.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/booking/bookings")
@Slf4j
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingDto> createBooking(@Valid @RequestBody BookingRequest request) {
        log.info("Creating booking for userId: {}", request.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<BookingDto>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bookingService.getAllBookings(pageable));
    }

    @GetMapping("/my/{userId}")
    public ResponseEntity<Page<BookingDto>> getBookingsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("Fetching bookings for userId: {}", userId);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bookingService.getBookingsByUser(userId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @GetMapping("/reference/{ref}")
    public ResponseEntity<BookingDto> getBookingByReference(@PathVariable String ref) {
        return ResponseEntity.ok(bookingService.getBookingByReference(ref));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<BookingDto> updateBookingStatus(@PathVariable Long id,
                                                           @Valid @RequestBody BookingStatusUpdateRequest request) {
        log.info("Updating status for booking id: {}", id);
        return ResponseEntity.ok(bookingService.updateBookingStatus(id, request.getStatus()));
    }

    @DeleteMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        log.info("Cancelling booking with id: {}", id);
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }
}
