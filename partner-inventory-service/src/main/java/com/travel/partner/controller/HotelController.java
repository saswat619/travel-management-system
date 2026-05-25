package com.travel.partner.controller;

import com.travel.partner.dto.HotelDto;
import com.travel.partner.dto.HotelRequest;
import com.travel.partner.projection.HotelSummary;
import com.travel.partner.service.HotelService;

import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/partner/hotels")
@Slf4j
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Page<HotelSummary>> getAllHotelSummaries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(hotelService.getAllHotelSummaries(pageable));
    }

    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<HotelDto> getHotelById(@PathVariable Long id) {
        return ResponseEntity.ok(hotelService.getHotelById(id));
    }

    @GetMapping(value = "/search", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Page<HotelDto>> searchHotelsByCity(
            @RequestParam String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<HotelDto> results = hotelService.searchHotelsByCity(city, pageable)
                .map(hotel -> hotelService.getHotelById(hotel.getId()));
        return ResponseEntity.ok(results);
    }

    // Search hotels by country and minimum star rating
    @GetMapping(value = "/search/country", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Page<HotelDto>> searchByCountryAndStars(
            @RequestParam String country,
            @RequestParam(defaultValue = "1") int minStars,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Searching hotels in country: {}, minStars: {}", country, minStars);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(hotelService.searchByCountryAndMinStars(country, minStars, pageable));
    }

    // Get all hotels managed by a specific partner
    @GetMapping(value = "/partner/{partnerId}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<List<HotelDto>> getHotelsByPartner(@PathVariable Long partnerId) {
        log.info("Fetching hotels for partnerId: {}", partnerId);
        return ResponseEntity.ok(hotelService.getHotelsByPartner(partnerId));
    }

    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAVEL_AGENT')")
    public ResponseEntity<HotelDto> createHotel(@Valid @RequestBody HotelRequest request) {
        log.info("Creating hotel: {}", request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(hotelService.createHotel(request));
    }

    @PutMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAVEL_AGENT')")
    public ResponseEntity<HotelDto> updateHotel(@PathVariable Long id,
                                                  @Valid @RequestBody HotelRequest request) {
        log.info("Updating hotel with id: {}", id);
        return ResponseEntity.ok(hotelService.updateHotel(id, request));
    }

    @PatchMapping("/{id}/rooms")
    public ResponseEntity<HotelDto> updateAvailableRooms(@PathVariable Long id, @RequestParam int delta) {
        log.info("Updating available rooms for hotel id: {}, delta: {}", id, delta);
        return ResponseEntity.ok(hotelService.updateAvailableRooms(id, delta));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long id) {
        log.info("Deleting hotel with id: {}", id);
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }
}
