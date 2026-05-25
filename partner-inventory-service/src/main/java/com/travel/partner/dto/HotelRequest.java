package com.travel.partner.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HotelRequest {

    @NotBlank(message = "Hotel name is required")
    private String name;

    private String location;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Country is required")
    private String country;

    @Min(value = 1, message = "Star rating must be at least 1")
    @Max(value = 5, message = "Star rating must not exceed 5")
    private Integer starRating;

    @Min(value = 1, message = "Total rooms must be at least 1")
    private Integer totalRooms;

    @Min(value = 0, message = "Available rooms cannot be negative")
    private Integer availableRooms;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price per night must be positive")
    private BigDecimal pricePerNight;

    private String amenities;

    private Long partnerId;
}
