package com.travel.booking.repository;

import com.travel.booking.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByBookingId(Long bookingId);

    Optional<Reservation> findByReservationReference(String reservationReference);
}
