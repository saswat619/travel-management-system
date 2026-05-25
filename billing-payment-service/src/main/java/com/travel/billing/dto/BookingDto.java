package com.travel.billing.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class BookingDto {

    private Long id;
    private String bookingReference;
    private Long userId;
    private BigDecimal totalAmount;
    private String status;
}
