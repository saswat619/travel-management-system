package com.travel.partner.projection;

import java.math.BigDecimal;

public interface HotelSummary {
    Long getId();
    String getName();
    String getCity();
    String getCountry();
    Integer getStarRating();
    BigDecimal getPricePerNight();
    Integer getAvailableRooms();
}
