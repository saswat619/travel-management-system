package com.travel.partner.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "transports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transport extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    // CAR_RENTAL, BUS, TRAIN, SHUTTLE, TRANSFER, TAXI
    @NotBlank
    private String transportType;

    @NotBlank
    private String vehicleName;

    private String vehicleModel;

    private String licensePlate;

    @Min(1)
    private Integer capacity;

    private String pickupLocation;

    private String dropoffLocation;

    private String coverageArea;

    @Column(precision = 10, scale = 2)
    private BigDecimal pricePerDay;

    @Column(precision = 10, scale = 2)
    private BigDecimal pricePerTrip;

    private Integer totalUnits;

    private Integer availableUnits;

    // AVAILABLE, BOOKED, MAINTENANCE, INACTIVE
    private String status;

    private String features;

    @Builder.Default
    private boolean active = true;

    @Version
    private Long version;
}
