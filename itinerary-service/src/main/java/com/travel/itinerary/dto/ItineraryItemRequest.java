package com.travel.itinerary.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class ItineraryItemRequest {

    @NotNull(message = "Day number is required")
    @Min(value = 1, message = "Day number must be at least 1")
    private Integer dayNumber;

    private LocalTime activityTime;

    @NotBlank(message = "Activity title is required")
    private String activityTitle;

    private String activityDescription;

    private String location;

    @NotBlank(message = "Activity type is required")
    private String activityType;

    private BigDecimal estimatedCost;

    private Double durationHours;

    private String notes;
}
