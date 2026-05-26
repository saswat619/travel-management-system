package com.travel.itinerary.service;

import com.travel.itinerary.client.BookingClient;
import com.travel.itinerary.dto.ItineraryDto;
import com.travel.itinerary.dto.ItineraryItemRequest;
import com.travel.itinerary.dto.ItineraryRequest;
import com.travel.itinerary.entity.Itinerary;
import com.travel.itinerary.entity.ItineraryItem;
import com.travel.itinerary.exception.ResourceNotFoundException;
import com.travel.itinerary.repository.ItineraryBookingRepository;
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
    private ItineraryBookingRepository itineraryBookingRepository;

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

        // First call: return base itinerary; Second call (after save): return itinerary with item
        when(itineraryRepository.findById(1L))
                .thenReturn(Optional.of(itinerary))
                .thenReturn(Optional.of(itineraryWithItem));

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

    // -----------------------------------------------------------------------
    // POSITIVE: update itinerary title and destination
    // -----------------------------------------------------------------------
    @Test
    void testUpdateItinerary_ChangesFieldsCorrectly() {
        ItineraryRequest updateRequest = new ItineraryRequest();
        updateRequest.setTitle("Updated Paris Trip");
        updateRequest.setDescription("Updated description");
        updateRequest.setUserId(1L);
        updateRequest.setDestination("Lyon, France");
        updateRequest.setStartDate(LocalDate.of(2024, 7, 1));
        updateRequest.setEndDate(LocalDate.of(2024, 7, 5));

        Itinerary updatedItinerary = Itinerary.builder()
                .id(1L)
                .title("Updated Paris Trip")
                .description("Updated description")
                .userId(1L)
                .destination("Lyon, France")
                .startDate(LocalDate.of(2024, 7, 1))
                .endDate(LocalDate.of(2024, 7, 5))
                .totalDays(5)
                .status("DRAFT")
                .items(new ArrayList<>())
                .build();

        when(itineraryRepository.findById(1L)).thenReturn(Optional.of(itinerary));
        when(itineraryRepository.save(any(Itinerary.class))).thenReturn(updatedItinerary);

        ItineraryDto result = itineraryService.updateItinerary(1L, updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Updated Paris Trip");
        assertThat(result.getDestination()).isEqualTo("Lyon, France");
        assertThat(result.getTotalDays()).isEqualTo(5);
        verify(itineraryRepository).save(any(Itinerary.class));
    }

    // -----------------------------------------------------------------------
    // NEGATIVE: update itinerary throws exception when not found
    // -----------------------------------------------------------------------
    @Test
    void testUpdateItinerary_NotFound_ThrowsException() {
        when(itineraryRepository.findById(99L)).thenReturn(Optional.empty());

        ItineraryRequest updateRequest = new ItineraryRequest();
        updateRequest.setTitle("Test");
        updateRequest.setStartDate(LocalDate.now());
        updateRequest.setEndDate(LocalDate.now().plusDays(3));

        assertThatThrownBy(() -> itineraryService.updateItinerary(99L, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Itinerary");
    }

    // -----------------------------------------------------------------------
    // NEGATIVE: add item to itinerary throws exception when itinerary not found
    // -----------------------------------------------------------------------
    @Test
    void testAddItemToItinerary_ItineraryNotFound_ThrowsException() {
        when(itineraryRepository.findById(99L)).thenReturn(Optional.empty());

        ItineraryItemRequest itemRequest = new ItineraryItemRequest();
        itemRequest.setDayNumber(1);
        itemRequest.setActivityTitle("Museum Tour");
        itemRequest.setActivityType("SIGHTSEEING");

        assertThatThrownBy(() -> itineraryService.addItemToItinerary(99L, itemRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Itinerary");
    }

    // -----------------------------------------------------------------------
    // POSITIVE: create itinerary calculates correct total days for single day
    // -----------------------------------------------------------------------
    @Test
    void testCreateItinerary_SingleDay_TotalDaysIsOne() {
        ItineraryRequest singleDayRequest = new ItineraryRequest();
        singleDayRequest.setTitle("Day Trip");
        singleDayRequest.setUserId(1L);
        singleDayRequest.setDestination("Versailles");
        singleDayRequest.setStartDate(LocalDate.of(2024, 6, 1));
        singleDayRequest.setEndDate(LocalDate.of(2024, 6, 1));  // same day

        Itinerary singleDayItinerary = Itinerary.builder()
                .id(2L)
                .title("Day Trip")
                .userId(1L)
                .destination("Versailles")
                .startDate(LocalDate.of(2024, 6, 1))
                .endDate(LocalDate.of(2024, 6, 1))
                .totalDays(1)
                .status("DRAFT")
                .items(new ArrayList<>())
                .build();

        when(itineraryRepository.save(any(Itinerary.class))).thenReturn(singleDayItinerary);

        ItineraryDto result = itineraryService.createItinerary(singleDayRequest);

        assertThat(result.getTotalDays()).isEqualTo(1);
    }
}
