package com.travel.booking.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String bookingReference;

    private Long userId;       // CustomerID

    private Long partnerId;    // PartnerID — which partner this booking is with

    // ItemType: FLIGHT, HOTEL, TRANSPORT, PACKAGE (what is being booked)
    private String itemType;

    private Long flightId;     // specific flight (if itemType=FLIGHT or combined)

    private Long hotelId;      // specific hotel  (if itemType=HOTEL  or combined)

    private LocalDate bookingDate;

    private LocalDate travelStartDate;

    private LocalDate travelEndDate;

    @Min(1)
    private Integer numberOfTravelers;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Column(columnDefinition = "TEXT")
    private String specialRequests;

    @Version
    private Long version;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reservation> reservations;
}
