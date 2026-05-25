package com.travel.analytics.repository;

import com.travel.analytics.entity.DashboardMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DashboardMetricRepository extends JpaRepository<DashboardMetric, Long> {

    Optional<DashboardMetric> findByMetricName(String name);

    List<DashboardMetric> findByCategory(String category);
}
