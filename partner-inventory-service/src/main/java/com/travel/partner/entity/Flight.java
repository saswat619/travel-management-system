package com.travel.partner.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "flights")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Flight extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @NotBlank
    private String flightNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @NotBlank
    private String origin;

    @NotBlank
    private String destination;

    @NotBlank
    private String originAirportCode;

    @NotBlank
    private String destinationAirportCode;

    private LocalDateTime departureTime;

    private LocalDateTime arrivalTime;

    private Integer durationMinutes;

    @Min(1)
    private Integer totalSeats;

    private Integer availableSeats;

    @Column(precision = 10, scale = 2)
    private BigDecimal priceEconomy;

    @Column(precision = 10, scale = 2)
    private BigDecimal priceBusiness;

    @Column(precision = 10, scale = 2)
    private BigDecimal priceFirstClass;

    // ECONOMY, BUSINESS, FIRST, ALL
    private String availableCabinClasses;

    private String aircraftType;

    // SCHEDULED, DELAYED, CANCELLED, COMPLETED, BOARDING
    private String status;

    @Builder.Default
    private boolean active = true;

    @Version
    private Long version;
}
