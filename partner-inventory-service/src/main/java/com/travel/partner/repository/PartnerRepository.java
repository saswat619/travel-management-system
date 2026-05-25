package com.travel.partner.repository;

import com.travel.partner.entity.Partner;
import com.travel.partner.projection.PartnerSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {

    Page<PartnerSummary> findAllProjectedBy(Pageable pageable);

    List<PartnerSummary> findByType(String type);

    Page<Partner> findByActiveTrue(Pageable pageable);

    Optional<Partner> findByName(String name);
}
