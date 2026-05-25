package com.travel.billing.client;

import com.travel.billing.dto.BookingDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "booking-service")
public interface BookingClient {

    @GetMapping("/api/booking/bookings/{id}")
    BookingDto getBookingById(@PathVariable Long id);

    @PutMapping("/api/booking/bookings/{id}/status")
    void updateBookingStatus(@PathVariable Long id, @RequestBody Object statusRequest);
}
