package com.travel.partner.repository;

import com.travel.partner.entity.Hotel;
import com.travel.partner.projection.HotelSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    Page<HotelSummary> findAllProjectedBy(Pageable pageable);

    Page<Hotel> findByCityAndActiveTrueOrderByStarRatingDesc(String city, Pageable pageable);

    Page<Hotel> findByCountryAndStarRatingGreaterThanEqualAndActiveTrue(String country, Integer minRating, Pageable pageable);

    List<Hotel> findByPartnerId(Long partnerId);
}
