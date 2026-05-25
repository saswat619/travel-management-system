package com.travel.booking.repository;

import com.travel.booking.entity.Booking;
import com.travel.booking.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.reservations WHERE b.id = :id")
    Optional<Booking> findByIdWithReservations(@Param("id") Long id);

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    Page<Booking> findByUserIdAndStatus(Long userId, BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.travelStartDate BETWEEN :start AND :end")
    Page<Booking> findByTravelDateRange(@Param("start") LocalDate start,
                                        @Param("end") LocalDate end,
                                        Pageable pageable);
}
