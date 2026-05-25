package com.travel.analytics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "booking-service")
public interface BookingAnalyticsClient {

    @GetMapping("/api/booking/bookings")
    Object getAllBookings(@RequestParam int page, @RequestParam int size);
}
