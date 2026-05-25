package com.travel.booking.dto;

import com.travel.booking.entity.BookingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    private Long id;
    private String bookingReference;
    private Long userId;
    private Long partnerId;
    private String itemType;
    private Long flightId;
    private Long hotelId;
    private LocalDate bookingDate;
    private LocalDate travelStartDate;
    private LocalDate travelEndDate;
    private Integer numberOfTravelers;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private String specialRequests;
    private List<ReservationDto> reservations;
    private LocalDateTime createdAt;
}
