package com.travel.partner.service;

import com.travel.partner.dto.PartnerDto;
import com.travel.partner.dto.PartnerRequest;
import com.travel.partner.entity.Partner;
import com.travel.partner.exception.ResourceNotFoundException;
import com.travel.partner.projection.PartnerSummary;
import com.travel.partner.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PartnerService {

    private final PartnerRepository partnerRepository;

    public Page<PartnerDto> getAllPartners(Pageable pageable) {
        log.debug("Fetching all partners with pagination: {}", pageable);
        return partnerRepository.findAll(pageable).map(this::mapToDto);
    }

    public Page<PartnerSummary> getAllPartnerSummaries(Pageable pageable) {
        log.debug("Fetching all partner summaries");
        return partnerRepository.findAllProjectedBy(pageable);
    }

    public PartnerDto getPartnerById(Long id) {
        log.debug("Fetching partner with id: {}", id);
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partner", "id", id));
        return mapToDto(partner);
    }

    public List<PartnerSummary> getPartnersByType(String type) {
        log.debug("Fetching partners by type: {}", type);
        return partnerRepository.findByType(type);
    }

    @Transactional
    public PartnerDto createPartner(PartnerRequest req) {
        log.info("Creating partner with name: {}", req.getName());
        Partner partner = Partner.builder()
                .name(req.getName())
                .type(req.getType())
                .status("ACTIVE")
                .contactEmail(req.getContactEmail())
                .contactPhone(req.getContactPhone())
                .address(req.getAddress())
                .website(req.getWebsite())
                .active(true)
                .build();
        Partner saved = partnerRepository.save(partner);
        log.info("Created partner with id: {}", saved.getId());
        return mapToDto(saved);
    }

    @Transactional
    public PartnerDto updatePartner(Long id, PartnerRequest req) {
        log.info("Updating partner with id: {}", id);
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partner", "id", id));
        partner.setName(req.getName());
        partner.setType(req.getType());
        if (req.getStatus() != null) partner.setStatus(req.getStatus());
        partner.setContactEmail(req.getContactEmail());
        partner.setContactPhone(req.getContactPhone());
        partner.setAddress(req.getAddress());
        partner.setWebsite(req.getWebsite());
        Partner saved = partnerRepository.save(partner);
        return mapToDto(saved);
    }

    @Transactional
    public void deletePartner(Long id) {
        log.info("Deleting partner with id: {}", id);
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partner", "id", id));
        partner.setActive(false);
        partnerRepository.save(partner);
    }

    private PartnerDto mapToDto(Partner partner) {
        return PartnerDto.builder()
                .id(partner.getId())
                .name(partner.getName())
                .type(partner.getType())
                .status(partner.getStatus())
                .contactEmail(partner.getContactEmail())
                .contactPhone(partner.getContactPhone())
                .address(partner.getAddress())
                .website(partner.getWebsite())
                .active(partner.isActive())
                .createdAt(partner.getCreatedAt())
                .build();
    }
}
