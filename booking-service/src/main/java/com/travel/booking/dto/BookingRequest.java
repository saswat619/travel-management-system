package com.travel.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    private Long partnerId;  // PartnerID — which partner this booking is with

    // ItemType: FLIGHT, HOTEL, TRANSPORT, PACKAGE
    private String itemType;

    private Long flightId;

    private Long hotelId;

    @NotNull(message = "Travel start date is required")
    private LocalDate travelStartDate;

    @NotNull(message = "Travel end date is required")
    private LocalDate travelEndDate;

    @Min(value = 1, message = "Number of travelers must be at least 1")
    private Integer numberOfTravelers;

    private String specialRequests;
}
