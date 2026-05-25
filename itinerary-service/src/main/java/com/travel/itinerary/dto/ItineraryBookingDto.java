package com.travel.itinerary.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryBookingDto {
    private Long id;
    private Long itineraryId;
    private Long bookingId;
    private String bookingReference;
    private String bookingType;
    private String description;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime linkedAt;
}
