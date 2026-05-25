package com.travel.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.booking.dto.BookingDto;
import com.travel.booking.dto.BookingRequest;
import com.travel.booking.entity.BookingStatus;
import com.travel.booking.security.JwtAuthenticationFilter;
import com.travel.booking.security.JwtUtil;
import com.travel.booking.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = BookingController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class))
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @WithMockUser(roles = "USER")
    void createBooking_POST_returns201() throws Exception {
        // Given
        BookingRequest request = new BookingRequest();
        request.setUserId(1L);
        request.setFlightId(10L);
        request.setTravelStartDate(LocalDate.now().plusDays(30));
        request.setTravelEndDate(LocalDate.now().plusDays(37));
        request.setNumberOfTravelers(2);

        BookingDto responseDto = BookingDto.builder()
                .id(1L)
                .bookingReference("BK-TESTREF1")
                .userId(1L)
                .flightId(10L)
                .travelStartDate(request.getTravelStartDate())
                .travelEndDate(request.getTravelEndDate())
                .numberOfTravelers(2)
                .totalAmount(new BigDecimal("1000.00"))
                .status(BookingStatus.PENDING)
                .reservations(new ArrayList<>())
                .build();

        when(bookingService.createBooking(any(BookingRequest.class))).thenReturn(responseDto);

        // When/Then
        mockMvc.perform(post("/api/booking/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingReference").value("BK-TESTREF1"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getBookingById_GET_returns200() throws Exception {
        // Given
        BookingDto dto = BookingDto.builder()
                .id(1L)
                .bookingReference("BK-TESTREF1")
                .userId(1L)
                .flightId(10L)
                .status(BookingStatus.CONFIRMED)
                .totalAmount(new BigDecimal("1500.00"))
                .reservations(new ArrayList<>())
                .build();

        when(bookingService.getBookingById(1L)).thenReturn(dto);

        // When/Then
        mockMvc.perform(get("/api/booking/bookings/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingReference").value("BK-TESTREF1"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }
}
