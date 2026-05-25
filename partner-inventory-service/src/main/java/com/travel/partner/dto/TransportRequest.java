package com.travel.partner.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransportRequest {

    @NotNull(message = "Partner ID is required")
    private Long partnerId;

    @NotBlank(message = "Transport type is required")
    private String transportType;

    @NotBlank(message = "Vehicle name is required")
    private String vehicleName;

    private String vehicleModel;
    private String licensePlate;

    @NotNull @Min(1)
    private Integer capacity;

    private String pickupLocation;
    private String dropoffLocation;
    private String coverageArea;

    @DecimalMin("0.01")
    private BigDecimal pricePerDay;

    @DecimalMin("0.01")
    private BigDecimal pricePerTrip;

    @NotNull @Min(1)
    private Integer totalUnits;

    @NotNull @Min(0)
    private Integer availableUnits;

    private String features;
}
