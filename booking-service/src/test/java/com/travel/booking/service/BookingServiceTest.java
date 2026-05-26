package com.travel.booking.service;

import com.travel.booking.client.PartnerInventoryClient;
import com.travel.booking.dto.BookingDto;
import com.travel.booking.dto.BookingRequest;
import com.travel.booking.dto.FlightDto;
import com.travel.booking.dto.HotelDto;
import com.travel.booking.entity.Booking;
import com.travel.booking.entity.BookingStatus;
import com.travel.booking.exception.InsufficientInventoryException;
import com.travel.booking.exception.ResourceNotFoundException;
import com.travel.booking.repository.BookingRepository;
import com.travel.booking.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PartnerInventoryClient partnerInventoryClient;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void createBooking_success() {
        // Given
        BookingRequest request = new BookingRequest();
        request.setUserId(1L);
        request.setFlightId(10L);
        request.setTravelStartDate(LocalDate.now().plusDays(30));
        request.setTravelEndDate(LocalDate.now().plusDays(37));
        request.setNumberOfTravelers(2);
        request.setSpecialRequests("Window seat please");

        FlightDto flightDto = new FlightDto();
        flightDto.setId(10L);
        flightDto.setFlightNumber("AI-202");
        flightDto.setPriceEconomy(new BigDecimal("500.00"));
        flightDto.setAvailableSeats(20);

        Booking savedBooking = Booking.builder()
                .id(1L)
                .bookingReference("BK-TESTREF1")
                .userId(1L)
                .flightId(10L)
                .bookingDate(LocalDate.now())
                .travelStartDate(request.getTravelStartDate())
                .travelEndDate(request.getTravelEndDate())
                .numberOfTravelers(2)
                .totalAmount(new BigDecimal("1000.00"))
                .status(BookingStatus.PENDING)
                .reservations(new ArrayList<>())
                .build();

        when(partnerInventoryClient.getFlightById(10L)).thenReturn(flightDto);
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        when(reservationRepository.save(any())).thenReturn(null);
        when(partnerInventoryClient.updateFlightSeats(anyLong(), anyInt())).thenReturn(flightDto);

        // When
        BookingDto result = bookingService.createBooking(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(result.getUserId()).isEqualTo(1L);
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(partnerInventoryClient, times(1)).getFlightById(10L);
        verify(partnerInventoryClient, times(1)).updateFlightSeats(10L, -2);
    }

    @Test
    void getBookingById_notFound_throwsException() {
        // Given
        when(bookingRepository.findByIdWithReservations(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> bookingService.getBookingById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Booking");

        verify(bookingRepository, times(1)).findByIdWithReservations(999L);
    }

    @Test
    void cancelBooking_changesStatusToCancelled() {
        // Given
        Booking booking = Booking.builder()
                .id(1L)
                .bookingReference("BK-TESTREF1")
                .userId(1L)
                .flightId(10L)
                .numberOfTravelers(2)
                .status(BookingStatus.CONFIRMED)
                .reservations(new ArrayList<>())
                .build();

        FlightDto flightDto = new FlightDto();
        flightDto.setAvailableSeats(22);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(reservationRepository.findByBookingId(1L)).thenReturn(new ArrayList<>());
        when(partnerInventoryClient.updateFlightSeats(anyLong(), anyInt())).thenReturn(flightDto);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        // When
        bookingService.cancelBooking(1L);

        // Then
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository, times(1)).save(booking);
        verify(partnerInventoryClient, times(1)).updateFlightSeats(10L, 2);
    }

    // -----------------------------------------------------------------------
    // NEGATIVE: booking fails when insufficient flight seats
    // -----------------------------------------------------------------------
    @Test
    void createBooking_insufficientSeats_throwsException() {
        BookingRequest request = new BookingRequest();
        request.setUserId(1L);
        request.setFlightId(10L);
        request.setTravelStartDate(LocalDate.now().plusDays(30));
        request.setTravelEndDate(LocalDate.now().plusDays(37));
        request.setNumberOfTravelers(5);

        FlightDto flightDto = new FlightDto();
        flightDto.setId(10L);
        flightDto.setFlightNumber("AI-202");
        flightDto.setPriceEconomy(new BigDecimal("500.00"));
        flightDto.setAvailableSeats(2); // only 2 seats but need 5

        when(partnerInventoryClient.getFlightById(10L)).thenReturn(flightDto);

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(InsufficientInventoryException.class);

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    // -----------------------------------------------------------------------
    // NEGATIVE: cancel booking throws exception when booking not found
    // -----------------------------------------------------------------------
    @Test
    void cancelBooking_notFound_throwsException() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBooking(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Booking");
    }

    // -----------------------------------------------------------------------
    // POSITIVE: create booking with both flight and hotel (PACKAGE booking)
    // -----------------------------------------------------------------------
    @Test
    void createBooking_withFlightAndHotel_createsPackageBooking() {
        BookingRequest request = new BookingRequest();
        request.setUserId(1L);
        request.setFlightId(10L);
        request.setHotelId(20L);
        request.setTravelStartDate(LocalDate.now().plusDays(30));
        request.setTravelEndDate(LocalDate.now().plusDays(37));
        request.setNumberOfTravelers(2);

        FlightDto flightDto = new FlightDto();
        flightDto.setId(10L);
        flightDto.setFlightNumber("AI-202");
        flightDto.setPriceEconomy(new BigDecimal("500.00"));
        flightDto.setAvailableSeats(20);

        HotelDto hotelDto = new HotelDto();
        hotelDto.setId(20L);
        hotelDto.setName("Paris Palace");
        hotelDto.setPricePerNight(new BigDecimal("150.00"));
        hotelDto.setAvailableRooms(5);

        Booking savedBooking = Booking.builder()
                .id(2L)
                .bookingReference("BK-PACKAGE1")
                .userId(1L)
                .flightId(10L)
                .hotelId(20L)
                .itemType("PACKAGE")
                .numberOfTravelers(2)
                .totalAmount(new BigDecimal("1000.00"))
                .status(BookingStatus.PENDING)
                .reservations(new ArrayList<>())
                .build();

        when(partnerInventoryClient.getFlightById(10L)).thenReturn(flightDto);
        when(partnerInventoryClient.getHotelById(20L)).thenReturn(hotelDto);
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        when(reservationRepository.save(any())).thenReturn(null);
        when(partnerInventoryClient.updateFlightSeats(anyLong(), anyInt())).thenReturn(flightDto);
        when(partnerInventoryClient.updateHotelRooms(anyLong(), anyInt())).thenReturn(hotelDto);

        BookingDto result = bookingService.createBooking(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        verify(partnerInventoryClient, times(1)).getHotelById(20L);
        verify(partnerInventoryClient, times(1)).updateHotelRooms(20L, -1);
    }

    // -----------------------------------------------------------------------
    // POSITIVE: update booking status to CONFIRMED
    // -----------------------------------------------------------------------
    @Test
    void updateBookingStatus_toConfirmed_success() {
        Booking booking = Booking.builder()
                .id(1L)
                .bookingReference("BK-TESTREF1")
                .userId(1L)
                .flightId(10L)
                .numberOfTravelers(2)
                .status(BookingStatus.PENDING)
                .reservations(new ArrayList<>())
                .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(reservationRepository.findByBookingId(1L)).thenReturn(new ArrayList<>());
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.updateBookingStatus(1L, BookingStatus.CONFIRMED);

        assertThat(result).isNotNull();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(bookingRepository).save(booking);
    }

    // -----------------------------------------------------------------------
    // NEGATIVE: update booking status throws exception when booking not found
    // -----------------------------------------------------------------------
    @Test
    void updateBookingStatus_notFound_throwsException() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.updateBookingStatus(999L, BookingStatus.CONFIRMED))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Booking");
    }
}
