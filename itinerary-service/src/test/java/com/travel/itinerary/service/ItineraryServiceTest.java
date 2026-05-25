package com.travel.itinerary.service;

import com.travel.itinerary.client.BookingClient;
import com.travel.itinerary.dto.ItineraryDto;
import com.travel.itinerary.dto.ItineraryItemRequest;
import com.travel.itinerary.dto.ItineraryRequest;
import com.travel.itinerary.entity.Itinerary;
import com.travel.itinerary.entity.ItineraryItem;
import com.travel.itinerary.exception.ResourceNotFoundException;
import com.travel.itinerary.repository.ItineraryItemRepository;
import com.travel.itinerary.repository.ItineraryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItineraryServiceTest {

    @Mock
    private ItineraryRepository itineraryRepository;

    @Mock
    private ItineraryItemRepository itineraryItemRepository;

    @Mock
    private BookingClient bookingClient;

    @InjectMocks
    private ItineraryService itineraryService;

    private ItineraryRequest itineraryRequest;
    private Itinerary itinerary;

    @BeforeEach
    void setUp() {
        itineraryRequest = new ItineraryRequest();
        itineraryRequest.setTitle("Paris Trip");
        itineraryRequest.setDescription("A lovely trip to Paris");
        itineraryRequest.setUserId(1L);
        itineraryRequest.setDestination("Paris, France");
        itineraryRequest.setStartDate(LocalDate.of(2024, 6, 1));
        itineraryRequest.setEndDate(LocalDate.of(2024, 6, 7));
        itineraryRequest.setNotes("Book hotels in advance");

        itinerary = Itinerary.builder()
                .id(1L)
                .title("Paris Trip")
                .description("A lovely trip to Paris")
                .userId(1L)
                .destination("Paris, France")
                .startDate(LocalDate.of(2024, 6, 1))
                .endDate(LocalDate.of(2024, 6, 7))
                .totalDays(7)
                .status("DRAFT")
                .items(new ArrayList<>())
                .build();
    }

    @Test
    void testCreateItinerary_VerifyTotalDaysCalculated() {
        when(itineraryRepository.save(any(Itinerary.class))).thenReturn(itinerary);

        ItineraryDto result = itineraryService.createItinerary(itineraryRequest);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Paris Trip");
        assertThat(result.getTotalDays()).isEqualTo(7);
        assertThat(result.getStatus()).isEqualTo("DRAFT");
        verify(itineraryRepository, times(1)).save(any(Itinerary.class));
    }

    @Test
    void testCreateItinerary_WithBookingId_BookingClientFails_StillCreates() {
        itineraryRequest.setBookingId(100L);
        when(bookingClient.getBookingById(100L)).thenThrow(new RuntimeException("Booking service unavailable"));
        when(itineraryRepository.save(any(Itinerary.class))).thenReturn(itinerary);

        ItineraryDto result = itineraryService.createItinerary(itineraryRequest);

        assertThat(result).isNotNull();
        verify(itineraryRepository, times(1)).save(any(Itinerary.class));
    }

    @Test
    void testGetItineraryById_NotFound_ThrowsException() {
        when(itineraryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itineraryService.getItineraryById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Itinerary");
    }

    @Test
    void testGetItineraryById_Found_ReturnsDto() {
        when(itineraryRepository.findById(1L)).thenReturn(Optional.of(itinerary));

        ItineraryDto result = itineraryService.getItineraryById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Paris Trip");
    }

    @Test
    void testAddItemToItinerary_VerifyItemAdded() {
        ItineraryItemRequest itemRequest = new ItineraryItemRequest();
        itemRequest.setDayNumber(1);
        itemRequest.setActivityTitle("Eiffel Tower Visit");
        itemRequest.setActivityType("SIGHTSEEING");

        Itinerary itineraryWithItem = Itinerary.builder()
                .id(1L)
                .title("Paris Trip")
                .userId(1L)
                .destination("Paris, France")
                .startDate(LocalDate.of(2024, 6, 1))
                .endDate(LocalDate.of(2024, 6, 7))
                .totalDays(7)
                .status("DRAFT")
                .items(new ArrayList<>())
                .build();

        ItineraryItem savedItem = ItineraryItem.builder()
                .id(1L)
                .itinerary(itinerary)
                .dayNumber(1)
                .activityTitle("Eiffel Tower Visit")
                .activityType("SIGHTSEEING")
                .build();
        itineraryWithItem.getItems().add(savedItem);

        when(itineraryRepository.findById(1L)).thenReturn(Optional.of(itinerary));
        when(itineraryRepository.save(any(Itinerary.class))).thenReturn(itineraryWithItem);

        ItineraryDto result = itineraryService.addItemToItinerary(1L, itemRequest);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getActivityTitle()).isEqualTo("Eiffel Tower Visit");
    }

    @Test
    void testDeleteItinerary_NotFound_ThrowsException() {
        when(itineraryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itineraryService.deleteItinerary(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
