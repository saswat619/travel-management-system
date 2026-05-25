package com.travel.itinerary.repository;

import com.travel.itinerary.entity.ItineraryBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItineraryBookingRepository extends JpaRepository<ItineraryBooking, Long> {

    List<ItineraryBooking> findByItineraryId(Long itineraryId);

    Optional<ItineraryBooking> findByItineraryIdAndBookingId(Long itineraryId, Long bookingId);

    List<ItineraryBooking> findByBookingId(Long bookingId);

    @Modifying
    @Query("DELETE FROM ItineraryBooking ib WHERE ib.itinerary.id = :itineraryId AND ib.bookingId = :bookingId")
    void deleteByItineraryIdAndBookingId(@Param("itineraryId") Long itineraryId, @Param("bookingId") Long bookingId);

    boolean existsByItineraryIdAndBookingId(Long itineraryId, Long bookingId);
}
