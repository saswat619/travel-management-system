package com.travel.booking.service;

import com.travel.booking.client.PartnerInventoryClient;
import com.travel.booking.dto.*;
import com.travel.booking.entity.Booking;
import com.travel.booking.entity.BookingStatus;
import com.travel.booking.entity.Reservation;
import com.travel.booking.exception.InsufficientInventoryException;
import com.travel.booking.exception.ResourceNotFoundException;
import com.travel.booking.repository.BookingRepository;
import com.travel.booking.repository.ReservationRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ReservationRepository reservationRepository;
    private final PartnerInventoryClient partnerInventoryClient;

    @Transactional
    @CircuitBreaker(name = "partnerService", fallbackMethod = "createBookingFallback")
    public BookingDto createBooking(BookingRequest req) {
        log.info("Creating booking for userId: {}, flightId: {}", req.getUserId(), req.getFlightId());

        // Fetch flight info and check seat availability
        FlightDto flightDto = partnerInventoryClient.getFlightById(req.getFlightId());
        if (flightDto.getAvailableSeats() == null || flightDto.getAvailableSeats() < req.getNumberOfTravelers()) {
            throw new InsufficientInventoryException("FLIGHT_SEAT", req.getFlightId());
        }

        // Generate booking reference
        String bookingReference = generateBookingReference();

        // Calculate total amount using economy price as base
        java.math.BigDecimal pricePerSeat = flightDto.getPriceEconomy() != null
                ? flightDto.getPriceEconomy()
                : java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalAmount = pricePerSeat.multiply(
                java.math.BigDecimal.valueOf(req.getNumberOfTravelers()));

        // Create booking
        // Derive itemType from what was provided
        String itemType = req.getItemType() != null ? req.getItemType()
                : (req.getFlightId() != null && req.getHotelId() != null) ? "PACKAGE"
                : (req.getFlightId() != null) ? "FLIGHT"
                : (req.getHotelId() != null) ? "HOTEL" : "FLIGHT";

        Booking booking = Booking.builder()
                .bookingReference(bookingReference)
                .userId(req.getUserId())
                .partnerId(req.getPartnerId())
                .itemType(itemType)
                .flightId(req.getFlightId())
                .hotelId(req.getHotelId())
                .bookingDate(LocalDate.now())
                .travelStartDate(req.getTravelStartDate())
                .travelEndDate(req.getTravelEndDate())
                .numberOfTravelers(req.getNumberOfTravelers())
                .totalAmount(totalAmount)
                .status(BookingStatus.PENDING)
                .specialRequests(req.getSpecialRequests())
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        List<Reservation> createdReservations = new ArrayList<>();

        // Create flight reservation
        Reservation flightReservation = Reservation.builder()
                .reservationReference(generateReservationRef())
                .booking(savedBooking)
                .itemType("FLIGHT")
                .itemId(req.getFlightId())
                .itemName(flightDto.getFlightNumber() != null
                        ? flightDto.getFlightNumber() + " " + flightDto.getOrigin() + "-" + flightDto.getDestination()
                        : "Flight")
                .checkInDate(req.getTravelStartDate())
                .checkOutDate(req.getTravelEndDate())
                .guestCount(req.getNumberOfTravelers())
                .pricePerUnit(pricePerSeat)
                .totalPrice(totalAmount)
                .status("PENDING")
                .build();
        createdReservations.add(reservationRepository.save(flightReservation));

        // Reserve hotel rooms if hotelId provided
        if (req.getHotelId() != null) {
            HotelDto hotelDto = partnerInventoryClient.getHotelById(req.getHotelId());
            if (hotelDto.getAvailableRooms() == null || hotelDto.getAvailableRooms() < 1) {
                throw new InsufficientInventoryException("HOTEL_ROOM", req.getHotelId());
            }
            java.math.BigDecimal hotelTotalPrice = hotelDto.getPricePerNight() != null
                    ? hotelDto.getPricePerNight()
                    : java.math.BigDecimal.ZERO;
            Reservation hotelReservation = Reservation.builder()
                    .reservationReference(generateReservationRef())
                    .booking(savedBooking)
                    .itemType("HOTEL")
                    .itemId(req.getHotelId())
                    .itemName(hotelDto.getName() != null ? hotelDto.getName() : "Hotel")
                    .checkInDate(req.getTravelStartDate())
                    .checkOutDate(req.getTravelEndDate())
                    .guestCount(req.getNumberOfTravelers())
                    .pricePerUnit(hotelDto.getPricePerNight())
                    .totalPrice(hotelTotalPrice)
                    .status("PENDING")
                    .build();
            createdReservations.add(reservationRepository.save(hotelReservation));
            // Decrement hotel rooms
            partnerInventoryClient.updateHotelRooms(req.getHotelId(), -1);
        }

        // Decrement flight seats
        partnerInventoryClient.updateFlightSeats(req.getFlightId(), -req.getNumberOfTravelers());

        log.info("Created booking with reference: {}", bookingReference);
        return mapToDto(savedBooking);
    }

    public BookingDto createBookingFallback(BookingRequest req, Exception e) {
        log.warn("Fallback triggered for createBooking: {}", e.getMessage());
        return BookingDto.builder()
                .bookingReference("PENDING-" + generateBookingReference())
                .userId(req.getUserId())
                .flightId(req.getFlightId())
                .status(BookingStatus.PENDING)
                .specialRequests("WARNING: Booking created in fallback mode. Inventory check unavailable.")
                .build();
    }

    @Transactional(readOnly = true)
    public Page<BookingDto> getAllBookings(Pageable pageable) {
        log.debug("Fetching all bookings");
        return bookingRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<BookingDto> getBookingsByUser(Long userId, Pageable pageable) {
        log.debug("Fetching bookings for userId: {}", userId);
        return bookingRepository.findByUserId(userId, pageable).map(this::mapToDto);
    }

    public BookingDto getBookingById(Long id) {
        log.debug("Fetching booking with id: {}", id);
        Booking booking = bookingRepository.findByIdWithReservations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
        return mapToDto(booking);
    }

    public BookingDto getBookingByReference(String ref) {
        log.debug("Fetching booking with reference: {}", ref);
        Booking booking = bookingRepository.findByBookingReference(ref)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "reference", ref));
        return mapToDto(booking);
    }

    @Transactional
    public BookingDto updateBookingStatus(Long id, BookingStatus status) {
        log.info("Updating booking status to {} for id: {}", status, id);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
        booking.setStatus(status);
        bookingRepository.save(booking);

        // Cascade status to all reservations
        List<Reservation> reservations = reservationRepository.findByBookingId(id);
        reservations.forEach(r -> r.setStatus(status.name()));
        reservationRepository.saveAll(reservations);

        return mapToDto(booking);
    }

    @Transactional
    @CircuitBreaker(name = "partnerService", fallbackMethod = "cancelBookingFallback")
    public void cancelBooking(Long id) {
        log.info("Cancelling booking with id: {}", id);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
        booking.setStatus(BookingStatus.CANCELLED);

        // Update reservations
        List<Reservation> reservations = reservationRepository.findByBookingId(id);
        reservations.forEach(r -> r.setStatus("CANCELLED"));
        reservationRepository.saveAll(reservations);

        // Restore flight seats
        if (booking.getFlightId() != null) {
            partnerInventoryClient.updateFlightSeats(booking.getFlightId(), booking.getNumberOfTravelers());
        }

        // Restore hotel rooms
        if (booking.getHotelId() != null) {
            partnerInventoryClient.updateHotelRooms(booking.getHotelId(), 1);
        }

        bookingRepository.save(booking);
        log.info("Booking {} cancelled successfully", id);
    }

    public void cancelBookingFallback(Long id, Exception e) {
        log.warn("Fallback triggered for cancelBooking id {}: {}", id, e.getMessage());
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    private BookingDto mapToDto(Booking booking) {
        List<ReservationDto> reservationDtos = reservationRepository.findByBookingId(booking.getId())
                .stream()
                .map(this::mapReservationToDto)
                .collect(Collectors.toList());

        return BookingDto.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .userId(booking.getUserId())
                .partnerId(booking.getPartnerId())
                .itemType(booking.getItemType())
                .flightId(booking.getFlightId())
                .hotelId(booking.getHotelId())
                .bookingDate(booking.getBookingDate())
                .travelStartDate(booking.getTravelStartDate())
                .travelEndDate(booking.getTravelEndDate())
                .numberOfTravelers(booking.getNumberOfTravelers())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .specialRequests(booking.getSpecialRequests())
                .reservations(reservationDtos)
                .createdAt(booking.getCreatedAt())
                .build();
    }

    private ReservationDto mapReservationToDto(Reservation reservation) {
        return ReservationDto.builder()
                .id(reservation.getId())
                .reservationReference(reservation.getReservationReference())
                .bookingId(reservation.getBooking() != null ? reservation.getBooking().getId() : null)
                .itemType(reservation.getItemType())
                .itemId(reservation.getItemId())
                .itemName(reservation.getItemName())
                .checkInDate(reservation.getCheckInDate())
                .checkOutDate(reservation.getCheckOutDate())
                .guestCount(reservation.getGuestCount())
                .pricePerUnit(reservation.getPricePerUnit())
                .totalPrice(reservation.getTotalPrice())
                .status(reservation.getStatus())
                .build();
    }

    private String generateBookingReference() {
        return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateReservationRef() {
        return "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
