package com.travel.partner.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;


@Entity
@Table(name = "partners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Partner extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String name;

    private String type; // HOTEL, AIRLINE, TOUR_OPERATOR, CAR_RENTAL

    // Status: ACTIVE, INACTIVE, SUSPENDED, PENDING
    @Builder.Default
    private String status = "ACTIVE";

    @Email
    private String contactEmail;

    private String contactPhone;

    private String address;

    private String website;

    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "partner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Flight> flights;

    @OneToMany(mappedBy = "partner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transport> transports;
}
