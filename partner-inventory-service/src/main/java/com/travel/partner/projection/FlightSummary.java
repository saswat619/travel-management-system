package com.travel.partner.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface FlightSummary {
    Long getId();
    String getFlightNumber();
    String getOrigin();
    String getDestination();
    String getOriginAirportCode();
    String getDestinationAirportCode();
    LocalDateTime getDepartureTime();
    LocalDateTime getArrivalTime();
    Integer getAvailableSeats();
    BigDecimal getPriceEconomy();
    String getStatus();
}
