package com.travel.itinerary.repository;

import com.travel.itinerary.entity.ItineraryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItineraryItemRepository extends JpaRepository<ItineraryItem, Long> {

    List<ItineraryItem> findByItineraryIdOrderByDayNumberAscActivityTimeAsc(Long itineraryId);

    List<ItineraryItem> findByItineraryIdAndDayNumber(Long itineraryId, Integer dayNumber);

    @Query("SELECT i FROM ItineraryItem i WHERE i.itinerary.id = :itId AND i.activityType = :type")
    List<ItineraryItem> findByItineraryIdAndActivityType(@Param("itId") Long itId, @Param("type") String type);
}
