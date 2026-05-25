package com.travel.booking.client;

import com.travel.booking.dto.FlightDto;
import com.travel.booking.dto.HotelDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PartnerInventoryClientFallbackFactory implements FallbackFactory<PartnerInventoryClient> {

    @Override
    public PartnerInventoryClient create(Throwable cause) {
        log.error("Fallback triggered for PartnerInventoryClient: {}", cause.getMessage());
        return new PartnerInventoryClient() {

            @Override
            public FlightDto getFlightById(Long id) {
                log.warn("Fallback: returning empty FlightDto for id: {}", id);
                return new FlightDto();
            }

            @Override
            public FlightDto updateFlightSeats(Long id, int delta) {
                log.warn("Fallback: cannot update seats for flight id: {}, delta: {}", id, delta);
                return new FlightDto();
            }

            @Override
            public HotelDto getHotelById(Long id) {
                log.warn("Fallback: returning empty HotelDto for id: {}", id);
                return new HotelDto();
            }

            @Override
            public HotelDto updateHotelRooms(Long id, int delta) {
                log.warn("Fallback: cannot update rooms for hotel id: {}, delta: {}", id, delta);
                return new HotelDto();
            }
        };
    }
}
