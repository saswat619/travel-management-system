package com.travel.partner.repository;

import com.travel.partner.entity.Transport;
import com.travel.partner.projection.TransportSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransportRepository extends JpaRepository<Transport, Long> {

    Page<TransportSummary> findAllProjectedBy(Pageable pageable);

    Page<Transport> findByTransportTypeAndActiveTrue(String transportType, Pageable pageable);

    Page<Transport> findByActiveTrueAndAvailableUnitsGreaterThan(Integer minUnits, Pageable pageable);

    Page<Transport> findByPartnerId(Long partnerId, Pageable pageable);

    @Query("SELECT t FROM Transport t WHERE t.coverageArea LIKE %:area% AND t.active = true")
    Page<Transport> findByCoverageArea(@Param("area") String area, Pageable pageable);

    @Query("SELECT t FROM Transport t WHERE t.pickupLocation = :pickup AND t.dropoffLocation = :dropoff AND t.active = true AND t.availableUnits > 0")
    Page<Transport> findAvailableByRoute(@Param("pickup") String pickup, @Param("dropoff") String dropoff, Pageable pageable);
}
