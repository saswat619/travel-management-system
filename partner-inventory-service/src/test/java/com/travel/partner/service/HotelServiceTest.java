package com.travel.partner.service;

import com.travel.partner.dto.HotelDto;
import com.travel.partner.dto.HotelRequest;
import com.travel.partner.entity.Hotel;
import com.travel.partner.entity.Partner;
import com.travel.partner.exception.ResourceNotFoundException;
import com.travel.partner.repository.HotelRepository;
import com.travel.partner.repository.PartnerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private PartnerRepository partnerRepository;

    @InjectMocks
    private HotelService hotelService;

    @Test
    void createHotel_success() {
        // Given
        HotelRequest request = new HotelRequest();
        request.setName("Test Hotel");
        request.setCity("Paris");
        request.setCountry("France");
        request.setStarRating(4);
        request.setTotalRooms(100);
        request.setAvailableRooms(80);
        request.setPricePerNight(new BigDecimal("150.00"));

        Hotel savedHotel = Hotel.builder()
                .id(1L)
                .name("Test Hotel")
                .city("Paris")
                .country("France")
                .starRating(4)
                .totalRooms(100)
                .availableRooms(80)
                .pricePerNight(new BigDecimal("150.00"))
                .active(true)
                .build();

        when(hotelRepository.save(any(Hotel.class))).thenReturn(savedHotel);

        // When
        HotelDto result = hotelService.createHotel(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Hotel");
        assertThat(result.getCity()).isEqualTo("Paris");
        assertThat(result.getStarRating()).isEqualTo(4);
        verify(hotelRepository, times(1)).save(any(Hotel.class));
    }

    @Test
    void getHotelById_notFound_throwsException() {
        // Given
        when(hotelRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> hotelService.getHotelById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Hotel");

        verify(hotelRepository, times(1)).findById(999L);
    }

    @Test
    void getHotelById_success() {
        // Given
        Partner partner = Partner.builder().id(1L).name("Test Partner").build();
        Hotel hotel = Hotel.builder()
                .id(1L)
                .name("Grand Hotel")
                .city("Rome")
                .country("Italy")
                .starRating(5)
                .pricePerNight(new BigDecimal("300.00"))
                .active(true)
                .partner(partner)
                .build();

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));

        // When
        HotelDto result = hotelService.getHotelById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Grand Hotel");
        assertThat(result.getPartnerId()).isEqualTo(1L);
        assertThat(result.getPartnerName()).isEqualTo("Test Partner");
    }
}
