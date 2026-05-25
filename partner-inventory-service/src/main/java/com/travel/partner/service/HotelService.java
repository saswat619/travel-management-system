package com.travel.partner.service;

import com.travel.partner.dto.HotelDto;
import com.travel.partner.dto.HotelRequest;
import com.travel.partner.entity.Hotel;
import com.travel.partner.entity.Partner;
import com.travel.partner.exception.ResourceNotFoundException;
import com.travel.partner.projection.HotelSummary;
import com.travel.partner.repository.HotelRepository;
import com.travel.partner.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;
    private final PartnerRepository partnerRepository;

    public Page<HotelSummary> getAllHotelSummaries(Pageable pageable) {
        log.debug("Fetching all hotel summaries");
        return hotelRepository.findAllProjectedBy(pageable);
    }

    public HotelDto getHotelById(Long id) {
        log.debug("Fetching hotel with id: {}", id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", id));
        return mapToDto(hotel);
    }

    public Page<Hotel> searchHotelsByCity(String city, Pageable pageable) {
        log.debug("Searching hotels in city: {}", city);
        return hotelRepository.findByCityAndActiveTrueOrderByStarRatingDesc(city, pageable);
    }

    public Page<HotelDto> searchByCountryAndMinStars(String country, int minStars, Pageable pageable) {
        log.debug("Searching hotels in country: {}, minStars: {}", country, minStars);
        return hotelRepository
                .findByCountryAndStarRatingGreaterThanEqualAndActiveTrue(country, minStars, pageable)
                .map(this::mapToDto);
    }

    public List<HotelDto> getHotelsByPartner(Long partnerId) {
        log.debug("Fetching hotels for partnerId: {}", partnerId);
        return hotelRepository.findByPartnerId(partnerId)
                .stream().map(this::mapToDto).toList();
    }

    @Transactional
    public HotelDto createHotel(HotelRequest req) {
        log.info("Creating hotel: {}", req.getName());
        Partner partner = null;
        if (req.getPartnerId() != null) {
            partner = partnerRepository.findById(req.getPartnerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Partner", "id", req.getPartnerId()));
        }
        Hotel hotel = Hotel.builder()
                .name(req.getName())
                .location(req.getLocation())
                .city(req.getCity())
                .country(req.getCountry())
                .starRating(req.getStarRating())
                .totalRooms(req.getTotalRooms())
                .availableRooms(req.getAvailableRooms())
                .pricePerNight(req.getPricePerNight())
                .amenities(req.getAmenities())
                .active(true)
                .partner(partner)
                .build();
        Hotel saved = hotelRepository.save(hotel);
        log.info("Created hotel with id: {}", saved.getId());
        return mapToDto(saved);
    }

    @Transactional
    public HotelDto updateHotel(Long id, HotelRequest req) {
        log.info("Updating hotel with id: {}", id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", id));
        hotel.setName(req.getName());
        hotel.setLocation(req.getLocation());
        hotel.setCity(req.getCity());
        hotel.setCountry(req.getCountry());
        hotel.setStarRating(req.getStarRating());
        hotel.setTotalRooms(req.getTotalRooms());
        hotel.setAvailableRooms(req.getAvailableRooms());
        hotel.setPricePerNight(req.getPricePerNight());
        hotel.setAmenities(req.getAmenities());
        if (req.getPartnerId() != null) {
            Partner partner = partnerRepository.findById(req.getPartnerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Partner", "id", req.getPartnerId()));
            hotel.setPartner(partner);
        }
        Hotel saved = hotelRepository.save(hotel);
        return mapToDto(saved);
    }

    @Transactional
    public HotelDto updateAvailableRooms(Long id, int delta) {
        log.info("Updating available rooms for hotel id: {}, delta: {}", id, delta);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", id));
        int newAvailable = hotel.getAvailableRooms() + delta;
        if (newAvailable < 0) {
            throw new IllegalStateException("Insufficient rooms for hotel id: " + id);
        }
        hotel.setAvailableRooms(newAvailable);
        return mapToDto(hotelRepository.save(hotel));
    }

    @Transactional
    public void deleteHotel(Long id) {
        log.info("Deleting hotel with id: {}", id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", id));
        hotel.setActive(false);
        hotelRepository.save(hotel);
    }

    private HotelDto mapToDto(Hotel hotel) {
        return HotelDto.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .location(hotel.getLocation())
                .city(hotel.getCity())
                .country(hotel.getCountry())
                .starRating(hotel.getStarRating())
                .totalRooms(hotel.getTotalRooms())
                .availableRooms(hotel.getAvailableRooms())
                .pricePerNight(hotel.getPricePerNight())
                .amenities(hotel.getAmenities())
                .active(hotel.isActive())
                .partnerId(hotel.getPartner() != null ? hotel.getPartner().getId() : null)
                .partnerName(hotel.getPartner() != null ? hotel.getPartner().getName() : null)
                .build();
    }
}
