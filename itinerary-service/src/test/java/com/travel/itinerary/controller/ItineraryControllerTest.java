package com.travel.itinerary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.itinerary.config.AuditConfig;
import com.travel.itinerary.dto.ItineraryDto;
import com.travel.itinerary.dto.ItineraryRequest;
import com.travel.itinerary.security.JwtAuthenticationFilter;
import com.travel.itinerary.security.JwtUtil;
import com.travel.itinerary.service.ItineraryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItineraryController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuditConfig.class)
    })
class ItineraryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItineraryService itineraryService;

    @MockBean
    private JwtUtil jwtUtil;

    private ItineraryDto itineraryDto;

    @BeforeEach
    void setUp() {
        itineraryDto = ItineraryDto.builder()
                .id(1L)
                .title("Paris Trip")
                .userId(1L)
                .destination("Paris, France")
                .startDate(LocalDate.of(2024, 6, 1))
                .endDate(LocalDate.of(2024, 6, 7))
                .totalDays(7)
                .status("DRAFT")
                .items(Collections.emptyList())
                .build();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetItineraryById_Returns200WithLinks() throws Exception {
        when(itineraryService.getItineraryById(1L)).thenReturn(itineraryDto);

        mockMvc.perform(get("/api/itinerary/itineraries/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Paris Trip"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateItinerary_Returns201() throws Exception {
        ItineraryRequest request = new ItineraryRequest();
        request.setTitle("Paris Trip");
        request.setUserId(1L);
        request.setDestination("Paris, France");
        request.setStartDate(LocalDate.of(2024, 6, 1));
        request.setEndDate(LocalDate.of(2024, 6, 7));

        when(itineraryService.createItinerary(any(ItineraryRequest.class))).thenReturn(itineraryDto);

        mockMvc.perform(post("/api/itinerary/itineraries")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Paris Trip"));
    }
}
