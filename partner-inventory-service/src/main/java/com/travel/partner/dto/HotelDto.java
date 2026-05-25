package com.travel.partner.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelDto {
    private Long id;
    private String name;
    private String location;
    private String city;
    private String country;
    private Integer starRating;
    private Integer totalRooms;
    private Integer availableRooms;
    private BigDecimal pricePerNight;
    private String amenities;
    private boolean active;
    private Long partnerId;
    private String partnerName;
}
