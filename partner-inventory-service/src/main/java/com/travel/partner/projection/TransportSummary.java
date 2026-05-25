package com.travel.partner.projection;

import java.math.BigDecimal;

public interface TransportSummary {
    Long getId();
    String getTransportType();
    String getVehicleName();
    String getPickupLocation();
    String getDropoffLocation();
    Integer getAvailableUnits();
    BigDecimal getPricePerDay();
    String getStatus();
}
