package com.travel.partner.repository;

import com.travel.partner.entity.Flight;
import com.travel.partner.projection.FlightSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    Optional<Flight> findByFlightNumber(String flightNumber);

    Optional<Flight> findByIdAndActiveTrue(Long id);

    Page<FlightSummary> findAllByActiveTrueOrderByDepartureTimeAsc(Pageable pageable);

    Page<FlightSummary> findAllProjectedBy(Pageable pageable);

    Page<Flight> findByActiveTrueOrderByDepartureTimeAsc(Pageable pageable);

    @Query("SELECT f FROM Flight f WHERE f.origin = :origin AND f.destination = :destination " +
           "AND f.departureTime >= :departureDate AND f.availableSeats >= :seats AND f.active = true")
    Page<Flight> searchFlights(@Param("origin") String origin,
                                @Param("destination") String destination,
                                @Param("departureDate") LocalDateTime departureDate,
                                @Param("seats") Integer seats,
                                Pageable pageable);

    Page<Flight> findByPartnerId(Long partnerId, Pageable pageable);

    Page<Flight> findByStatus(String status, Pageable pageable);

    @Query("SELECT f FROM Flight f WHERE f.originAirportCode = :code OR f.destinationAirportCode = :code")
    Page<Flight> findByAirportCode(@Param("code") String airportCode, Pageable pageable);
}
