package com.travel.itinerary.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "itinerary_bookings",
       uniqueConstraints = @UniqueConstraint(columnNames = {"itinerary_id", "booking_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EntityListeners(AuditingEntityListener.class)
public class ItineraryBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id", nullable = false)
    private Itinerary itinerary;

    // References booking-service booking ID (cross-service reference, stored as plain Long)
    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    // Booking reference string e.g. "BK-ABC123" - stored for display without cross-service call
    private String bookingReference;

    // Type of booking: FLIGHT / HOTEL / TRANSPORT / PACKAGE
    private String bookingType;

    // Key details stored locally to avoid constant cross-service calls
    private String description;
    private String destination;
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime linkedAt;
}
