package com.travel.itinerary.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {

    private Long id;
    private String bookingReference;
    private Long userId;
    private Long packageId;
    private LocalDate travelStartDate;
    private LocalDate travelEndDate;
    private String status;
}
