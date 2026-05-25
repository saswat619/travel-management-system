package com.travel.partner.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FlightRequest {

    @NotBlank(message = "Flight number is required")
    private String flightNumber;

    @NotNull(message = "Partner ID is required")
    private Long partnerId;

    @NotBlank(message = "Origin is required")
    private String origin;

    @NotBlank(message = "Destination is required")
    private String destination;

    @NotBlank(message = "Origin airport code is required")
    private String originAirportCode;

    @NotBlank(message = "Destination airport code is required")
    private String destinationAirportCode;

    @NotNull(message = "Departure time is required")
    private LocalDateTime departureTime;

    @NotNull(message = "Arrival time is required")
    private LocalDateTime arrivalTime;

    private Integer durationMinutes;

    @NotNull @Min(1)
    private Integer totalSeats;

    @NotNull @Min(1)
    private Integer availableSeats;

    @NotNull @DecimalMin("0.01")
    private BigDecimal priceEconomy;

    private BigDecimal priceBusiness;
    private BigDecimal priceFirstClass;
    private String availableCabinClasses;
    private String aircraftType;
}
