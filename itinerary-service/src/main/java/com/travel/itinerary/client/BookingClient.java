package com.travel.itinerary.client;

import com.travel.itinerary.dto.BookingDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "booking-service")
public interface BookingClient {

    @GetMapping("/api/booking/bookings/{id}")
    BookingDto getBookingById(@PathVariable Long id);
}
