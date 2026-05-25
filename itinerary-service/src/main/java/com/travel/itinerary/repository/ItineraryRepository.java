package com.travel.itinerary.repository;

import com.travel.itinerary.entity.Itinerary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {

    Page<Itinerary> findByUserId(Long userId, Pageable pageable);

    Page<Itinerary> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    Page<Itinerary> findByDestinationContainingIgnoreCase(String destination, Pageable pageable);

    Optional<Itinerary> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT i FROM Itinerary i WHERE i.startDate >= :startDate ORDER BY i.startDate")
    Page<Itinerary> findUpcomingItineraries(@Param("startDate") LocalDate startDate, Pageable pageable);
}
