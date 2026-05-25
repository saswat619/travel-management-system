package com.travel.itinerary.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class LinkBookingRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    private String bookingReference;

    // FLIGHT / HOTEL / TRANSPORT / PACKAGE
    private String bookingType;

    private String description;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
}
