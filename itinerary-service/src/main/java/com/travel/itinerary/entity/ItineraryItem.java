package com.travel.itinerary.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "itinerary_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryItem extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id", nullable = false)
    private Itinerary itinerary;

    @Min(1)
    private Integer dayNumber;

    private LocalTime activityTime;

    @NotBlank
    @Column(nullable = false)
    private String activityTitle;

    @Column(columnDefinition = "TEXT")
    private String activityDescription;

    private String location;

    private String activityType;

    private BigDecimal estimatedCost;

    private Double durationHours;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    private boolean completed = false;
}
