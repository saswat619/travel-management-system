package com.travel.itinerary.service;

import com.travel.itinerary.client.BookingClient;
import com.travel.itinerary.dto.*;
import com.travel.itinerary.entity.Itinerary;
import com.travel.itinerary.entity.ItineraryBooking;
import com.travel.itinerary.entity.ItineraryItem;
import com.travel.itinerary.exception.ResourceNotFoundException;
import com.travel.itinerary.repository.ItineraryBookingRepository;
import com.travel.itinerary.repository.ItineraryItemRepository;
import com.travel.itinerary.repository.ItineraryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final ItineraryItemRepository itineraryItemRepository;
    private final ItineraryBookingRepository itineraryBookingRepository;
    private final BookingClient bookingClient;

    public Page<ItineraryDto> getAllItineraries(Pageable pageable) {
        return itineraryRepository.findAll(pageable).map(this::mapToDto);
    }

    public Page<ItineraryDto> getItinerariesByUser(Long userId, Pageable pageable) {
        return itineraryRepository.findByUserId(userId, pageable).map(this::mapToDto);
    }

    public Page<ItineraryDto> getItinerariesByUserAndStatus(Long userId, String status, Pageable pageable) {
        return itineraryRepository.findByUserIdAndStatus(userId, status, pageable).map(this::mapToDto);
    }

    public ItineraryDto getItineraryById(Long id) {
        return itineraryRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary", "id", id));
    }

    public Page<ItineraryDto> searchByDestination(String destination, Pageable pageable) {
        return itineraryRepository.findByDestinationContainingIgnoreCase(destination, pageable).map(this::mapToDto);
    }

    public Page<ItineraryDto> getUpcomingItineraries(Pageable pageable) {
        return itineraryRepository.findUpcomingItineraries(LocalDate.now(), pageable).map(this::mapToDto);
    }

    @Transactional
    public ItineraryDto createItinerary(ItineraryRequest req) {
        log.info("Creating itinerary: {} for user: {}", req.getTitle(), req.getUserId());

        long totalDays = ChronoUnit.DAYS.between(req.getStartDate(), req.getEndDate()) + 1;

        Itinerary itinerary = Itinerary.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .userId(req.getUserId())
                .destination(req.getDestination())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .totalDays((int) totalDays)
                .status("DRAFT")
                .notes(req.getNotes())
                .build();

        // If a bookingId is provided, try to fetch booking details
        if (req.getBookingId() != null) {
            try {
                BookingDto booking = bookingClient.getBookingById(req.getBookingId());
                ItineraryBooking link = ItineraryBooking.builder()
                        .itinerary(itinerary)
                        .bookingId(req.getBookingId())
                        .bookingReference(booking.getBookingReference())
                        .bookingType("PACKAGE")
                        .destination(itinerary.getDestination())
                        .startDate(booking.getTravelStartDate())
                        .endDate(booking.getTravelEndDate())
                        .description("Auto-linked from booking " + booking.getBookingReference())
                        .build();
                itinerary.getBookings().add(link);
            } catch (Exception e) {
                log.warn("Could not fetch booking details for id {}: {}", req.getBookingId(), e.getMessage());
            }
        }

        return mapToDto(itineraryRepository.save(itinerary));
    }

    @Transactional
    public ItineraryDto updateItinerary(Long id, ItineraryRequest req) {
        Itinerary itinerary = itineraryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary", "id", id));

        long totalDays = ChronoUnit.DAYS.between(req.getStartDate(), req.getEndDate()) + 1;
        itinerary.setTitle(req.getTitle());
        itinerary.setDescription(req.getDescription());
        itinerary.setDestination(req.getDestination());
        itinerary.setStartDate(req.getStartDate());
        itinerary.setEndDate(req.getEndDate());
        itinerary.setTotalDays((int) totalDays);
        itinerary.setNotes(req.getNotes());

        return mapToDto(itineraryRepository.save(itinerary));
    }

    // === BOOKING LINK MANAGEMENT (Bookings[] from Travel360 spec) ===

    @Transactional
    public ItineraryDto linkBooking(Long itineraryId, LinkBookingRequest req) {
        log.info("Linking booking {} to itinerary {}", req.getBookingId(), itineraryId);
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary", "id", itineraryId));

        if (itineraryBookingRepository.existsByItineraryIdAndBookingId(itineraryId, req.getBookingId())) {
            throw new RuntimeException("Booking " + req.getBookingId() + " is already linked to this itinerary");
        }

        // Try to fetch booking details from booking-service
        String bookingRef = req.getBookingReference();
        try {
            BookingDto booking = bookingClient.getBookingById(req.getBookingId());
            if (bookingRef == null) bookingRef = booking.getBookingReference();
        } catch (Exception e) {
            log.warn("Could not fetch booking details: {}", e.getMessage());
        }

        ItineraryBooking link = ItineraryBooking.builder()
                .itinerary(itinerary)
                .bookingId(req.getBookingId())
                .bookingReference(bookingRef)
                .bookingType(req.getBookingType())
                .description(req.getDescription())
                .destination(req.getDestination())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .build();

        itineraryBookingRepository.save(link);
        return mapToDto(itineraryRepository.findById(itineraryId).get());
    }

    @Transactional
    public ItineraryDto unlinkBooking(Long itineraryId, Long bookingId) {
        log.info("Unlinking booking {} from itinerary {}", bookingId, itineraryId);
        if (!itineraryRepository.existsById(itineraryId)) {
            throw new ResourceNotFoundException("Itinerary", "id", itineraryId);
        }
        itineraryBookingRepository.deleteByItineraryIdAndBookingId(itineraryId, bookingId);
        return mapToDto(itineraryRepository.findById(itineraryId).get());
    }

    public List<ItineraryBookingDto> getItineraryBookings(Long itineraryId) {
        return itineraryBookingRepository.findByItineraryId(itineraryId)
                .stream().map(this::mapBookingToDto).collect(Collectors.toList());
    }

    // === ITEM MANAGEMENT ===

    @Transactional
    public ItineraryDto addItemToItinerary(Long itineraryId, ItineraryItemRequest req) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary", "id", itineraryId));

        ItineraryItem item = ItineraryItem.builder()
                .itinerary(itinerary)
                .dayNumber(req.getDayNumber())
                .activityTime(req.getActivityTime())
                .activityTitle(req.getActivityTitle())
                .activityDescription(req.getActivityDescription())
                .location(req.getLocation())
                .activityType(req.getActivityType())
                .estimatedCost(req.getEstimatedCost())
                .durationHours(req.getDurationHours())
                .notes(req.getNotes())
                .completed(false)
                .build();

        itineraryItemRepository.save(item);
        return mapToDto(itineraryRepository.findById(itineraryId).get());
    }

    @Transactional
    public ItineraryDto updateItineraryItem(Long itineraryId, Long itemId, ItineraryItemRequest req) {
        ItineraryItem item = itineraryItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("ItineraryItem", "id", itemId));
        item.setDayNumber(req.getDayNumber());
        item.setActivityTime(req.getActivityTime());
        item.setActivityTitle(req.getActivityTitle());
        item.setActivityDescription(req.getActivityDescription());
        item.setLocation(req.getLocation());
        item.setActivityType(req.getActivityType());
        item.setEstimatedCost(req.getEstimatedCost());
        item.setDurationHours(req.getDurationHours());
        item.setNotes(req.getNotes());
        itineraryItemRepository.save(item);
        return mapToDto(itineraryRepository.findById(itineraryId).get());
    }

    @Transactional
    public void removeItemFromItinerary(Long itineraryId, Long itemId) {
        ItineraryItem item = itineraryItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("ItineraryItem", "id", itemId));
        itineraryItemRepository.delete(item);
    }

    @Transactional
    public ItineraryDto markItemCompleted(Long itineraryId, Long itemId, boolean completed) {
        ItineraryItem item = itineraryItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("ItineraryItem", "id", itemId));
        item.setCompleted(completed);
        itineraryItemRepository.save(item);
        return mapToDto(itineraryRepository.findById(itineraryId).get());
    }

    @Transactional
    public void deleteItinerary(Long id) {
        Itinerary itinerary = itineraryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary", "id", id));
        itineraryRepository.delete(itinerary);
    }

    private ItineraryDto mapToDto(Itinerary i) {
        return ItineraryDto.builder()
                .id(i.getId())
                .title(i.getTitle())
                .description(i.getDescription())
                .userId(i.getUserId())
                .destination(i.getDestination())
                .startDate(i.getStartDate())
                .endDate(i.getEndDate())
                .totalDays(i.getTotalDays())
                .status(i.getStatus())
                .notes(i.getNotes())
                .bookings(i.getBookings().stream().map(this::mapBookingToDto).collect(Collectors.toList()))
                .items(i.getItems().stream().map(this::mapItemToDto).collect(Collectors.toList()))
                .createdAt(i.getCreatedAt())
                .createdBy(i.getCreatedBy())
                .build();
    }

    private ItineraryBookingDto mapBookingToDto(ItineraryBooking b) {
        return ItineraryBookingDto.builder()
                .id(b.getId())
                .itineraryId(b.getItinerary().getId())
                .bookingId(b.getBookingId())
                .bookingReference(b.getBookingReference())
                .bookingType(b.getBookingType())
                .description(b.getDescription())
                .destination(b.getDestination())
                .startDate(b.getStartDate())
                .endDate(b.getEndDate())
                .linkedAt(b.getLinkedAt())
                .build();
    }

    private ItineraryItemDto mapItemToDto(ItineraryItem item) {
        return ItineraryItemDto.builder()
                .id(item.getId())
                .itineraryId(item.getItinerary().getId())
                .dayNumber(item.getDayNumber())
                .activityTime(item.getActivityTime())
                .activityTitle(item.getActivityTitle())
                .activityDescription(item.getActivityDescription())
                .location(item.getLocation())
                .activityType(item.getActivityType())
                .estimatedCost(item.getEstimatedCost())
                .durationHours(item.getDurationHours())
                .notes(item.getNotes())
                .completed(item.isCompleted())
                .build();
    }
}
