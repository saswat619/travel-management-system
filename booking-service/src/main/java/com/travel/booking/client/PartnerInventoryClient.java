package com.travel.booking.client;

import com.travel.booking.dto.FlightDto;
import com.travel.booking.dto.HotelDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "partner-inventory-service", fallbackFactory = PartnerInventoryClientFallbackFactory.class)
public interface PartnerInventoryClient {

    @GetMapping("/api/partner/flights/{id}")
    FlightDto getFlightById(@PathVariable Long id);

    @PatchMapping("/api/partner/flights/{id}/seats")
    FlightDto updateFlightSeats(@PathVariable Long id, @RequestParam int delta);

    @GetMapping("/api/partner/hotels/{id}")
    HotelDto getHotelById(@PathVariable Long id);

    @PatchMapping("/api/partner/hotels/{id}/rooms")
    HotelDto updateHotelRooms(@PathVariable Long id, @RequestParam int delta);
}
