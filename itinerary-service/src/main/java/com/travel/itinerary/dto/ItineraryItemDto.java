package com.travel.itinerary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryItemDto {

    private Long id;
    private Long itineraryId;
    private Integer dayNumber;
    private LocalTime activityTime;
    private String activityTitle;
    private String activityDescription;
    private String location;
    private String activityType;
    private BigDecimal estimatedCost;
    private Double durationHours;
    private String notes;
    private boolean completed;
}
