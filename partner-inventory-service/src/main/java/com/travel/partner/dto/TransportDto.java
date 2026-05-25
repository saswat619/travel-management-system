package com.travel.partner.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportDto {
    private Long id;
    private Long partnerId;
    private String partnerName;
    private String transportType;
    private String vehicleName;
    private String vehicleModel;
    private String licensePlate;
    private Integer capacity;
    private String pickupLocation;
    private String dropoffLocation;
    private String coverageArea;
    private BigDecimal pricePerDay;
    private BigDecimal pricePerTrip;
    private Integer totalUnits;
    private Integer availableUnits;
    private String status;
    private String features;
    private boolean active;
    private LocalDateTime createdAt;
}
