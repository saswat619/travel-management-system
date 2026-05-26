package com.travel.partner.service;

import com.travel.partner.dto.FlightDto;
import com.travel.partner.dto.FlightRequest;
import com.travel.partner.entity.Flight;
import com.travel.partner.entity.Partner;
import com.travel.partner.exception.ResourceNotFoundException;
import com.travel.partner.repository.FlightRepository;
import com.travel.partner.repository.PartnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightServiceTest {

    @Mock private FlightRepository flightRepository;
    @Mock private PartnerRepository partnerRepository;
    @InjectMocks private FlightService flightService;

    private Partner mockPartner;
    private Flight mockFlight;

    @BeforeEach
    void setUp() {
        mockPartner = Partner.builder().id(1L).name("Air India").type("AIRLINE").build();
        mockFlight = Flight.builder()
                .id(1L).flightNumber("AI-101").partner(mockPartner)
                .origin("Mumbai").destination("Delhi")
                .originAirportCode("BOM").destinationAirportCode("DEL")
                .departureTime(LocalDateTime.now().plusDays(1))
                .arrivalTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .totalSeats(180).availableSeats(50)
                .priceEconomy(new BigDecimal("4999.00"))
                .status("SCHEDULED").active(true).build();
    }

    @Test
    void testCreateFlight_Success() {
        FlightRequest req = new FlightRequest();
        req.setFlightNumber("AI-101");
        req.setPartnerId(1L);
        req.setOrigin("Mumbai");
        req.setDestination("Delhi");
        req.setOriginAirportCode("BOM");
        req.setDestinationAirportCode("DEL");
        req.setDepartureTime(LocalDateTime.now().plusDays(1));
        req.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(2));
        req.setTotalSeats(180);
        req.setAvailableSeats(180);
        req.setPriceEconomy(new BigDecimal("4999.00"));

        when(partnerRepository.findById(1L)).thenReturn(Optional.of(mockPartner));
        when(flightRepository.save(any(Flight.class))).thenReturn(mockFlight);

        FlightDto result = flightService.createFlight(req);

        assertThat(result).isNotNull();
        assertThat(result.getFlightNumber()).isEqualTo("AI-101");
        assertThat(result.getAirlineName()).isEqualTo("Air India");
        verify(flightRepository, times(1)).save(any(Flight.class));
    }

    @Test
    void testGetFlightById_NotFound() {
        when(flightRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> flightService.getFlightById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testUpdateFlightStatus() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(mockFlight));
        when(flightRepository.save(any(Flight.class))).thenReturn(mockFlight);

        FlightDto result = flightService.updateFlightStatus(1L, "BOARDING");

        verify(flightRepository).save(any(Flight.class));
        assertThat(result).isNotNull();
    }

    @Test
    void testDeleteFlight_SoftDelete() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(mockFlight));
        when(flightRepository.save(any(Flight.class))).thenReturn(mockFlight);

        flightService.deleteFlight(1L);

        verify(flightRepository).save(argThat(f -> !f.isActive()));
    }
}
