package com.travel.itinerary.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryDto {
    private Long id;
    private String title;
    private String description;
    private Long userId;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private String status;
    private String notes;
    private List<ItineraryBookingDto> bookings;
    private List<ItineraryItemDto> items;
    private LocalDateTime createdAt;
    private String createdBy;
}
