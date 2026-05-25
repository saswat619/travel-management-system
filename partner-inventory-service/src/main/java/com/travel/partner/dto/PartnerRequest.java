package com.travel.partner.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PartnerRequest {

    @NotBlank(message = "Partner name is required")
    private String name;

    private String type; // HOTEL, AIRLINE, TOUR_OPERATOR, CAR_RENTAL

    // ACTIVE, INACTIVE, SUSPENDED, PENDING (defaults to ACTIVE on create)
    private String status;

    @Email(message = "Valid email is required")
    private String contactEmail;

    private String contactPhone;

    private String address;

    private String website;
}
