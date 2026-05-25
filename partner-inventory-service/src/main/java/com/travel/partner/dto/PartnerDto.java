package com.travel.partner.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerDto {
    private Long id;
    private String name;
    private String type;
    private String status;   // ACTIVE, INACTIVE, SUSPENDED, PENDING
    private String contactEmail;
    private String contactPhone;
    private String address;
    private String website;
    private boolean active;
    private LocalDateTime createdAt;
}
