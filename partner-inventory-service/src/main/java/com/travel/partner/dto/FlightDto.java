package com.travel.partner.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightDto {
    private Long id;
    private String flightNumber;
    private Long partnerId;
    private String airlineName;
    private String origin;
    private String destination;
    private String originAirportCode;
    private String destinationAirportCode;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Integer durationMinutes;
    private Integer totalSeats;
    private Integer availableSeats;
    private BigDecimal priceEconomy;
    private BigDecimal priceBusiness;
    private BigDecimal priceFirstClass;
    private String availableCabinClasses;
    private String aircraftType;
    private String status;
    private boolean active;
    private java.time.LocalDateTime createdAt;
}
