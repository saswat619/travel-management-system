package com.travel.partner.service;

import com.travel.partner.dto.TransportDto;
import com.travel.partner.dto.TransportRequest;
import com.travel.partner.entity.Partner;
import com.travel.partner.entity.Transport;
import com.travel.partner.exception.ResourceNotFoundException;
import com.travel.partner.projection.TransportSummary;
import com.travel.partner.repository.PartnerRepository;
import com.travel.partner.repository.TransportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransportService {

    private final TransportRepository transportRepository;
    private final PartnerRepository partnerRepository;

    public Page<TransportSummary> getAllTransportSummaries(Pageable pageable) {
        return transportRepository.findAllProjectedBy(pageable);
    }

    public Page<TransportDto> getAvailableTransports(Pageable pageable) {
        return transportRepository.findByActiveTrueAndAvailableUnitsGreaterThan(0, pageable)
                .map(this::mapToDto);
    }

    public TransportDto getTransportById(Long id) {
        log.info("Fetching transport with id: {}", id);
        return transportRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Transport", "id", id));
    }

    public Page<TransportDto> getTransportsByType(String type, Pageable pageable) {
        return transportRepository.findByTransportTypeAndActiveTrue(type, pageable)
                .map(this::mapToDto);
    }

    public Page<TransportDto> getTransportsByPartner(Long partnerId, Pageable pageable) {
        return transportRepository.findByPartnerId(partnerId, pageable).map(this::mapToDto);
    }

    public Page<TransportDto> searchByRoute(String pickup, String dropoff, Pageable pageable) {
        log.info("Searching transports from {} to {}", pickup, dropoff);
        return transportRepository.findAvailableByRoute(pickup, dropoff, pageable)
                .map(this::mapToDto);
    }

    public Page<TransportDto> searchByCoverageArea(String area, Pageable pageable) {
        return transportRepository.findByCoverageArea(area, pageable).map(this::mapToDto);
    }

    @Transactional
    public TransportDto createTransport(TransportRequest req) {
        log.info("Creating transport: {}", req.getVehicleName());
        Partner partner = partnerRepository.findById(req.getPartnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner", "id", req.getPartnerId()));

        Transport transport = Transport.builder()
                .partner(partner)
                .transportType(req.getTransportType())
                .vehicleName(req.getVehicleName())
                .vehicleModel(req.getVehicleModel())
                .licensePlate(req.getLicensePlate())
                .capacity(req.getCapacity())
                .pickupLocation(req.getPickupLocation())
                .dropoffLocation(req.getDropoffLocation())
                .coverageArea(req.getCoverageArea())
                .pricePerDay(req.getPricePerDay())
                .pricePerTrip(req.getPricePerTrip())
                .totalUnits(req.getTotalUnits())
                .availableUnits(req.getAvailableUnits())
                .status("AVAILABLE")
                .features(req.getFeatures())
                .active(true)
                .build();

        return mapToDto(transportRepository.save(transport));
    }

    @Transactional
    public TransportDto updateTransport(Long id, TransportRequest req) {
        log.info("Updating transport with id: {}", id);
        Transport transport = transportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transport", "id", id));

        Partner partner = partnerRepository.findById(req.getPartnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner", "id", req.getPartnerId()));

        transport.setPartner(partner);
        transport.setTransportType(req.getTransportType());
        transport.setVehicleName(req.getVehicleName());
        transport.setVehicleModel(req.getVehicleModel());
        transport.setLicensePlate(req.getLicensePlate());
        transport.setCapacity(req.getCapacity());
        transport.setPickupLocation(req.getPickupLocation());
        transport.setDropoffLocation(req.getDropoffLocation());
        transport.setCoverageArea(req.getCoverageArea());
        transport.setPricePerDay(req.getPricePerDay());
        transport.setPricePerTrip(req.getPricePerTrip());
        transport.setTotalUnits(req.getTotalUnits());
        transport.setAvailableUnits(req.getAvailableUnits());
        transport.setFeatures(req.getFeatures());

        return mapToDto(transportRepository.save(transport));
    }

    @Transactional
    public void deleteTransport(Long id) {
        Transport transport = transportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transport", "id", id));
        transport.setActive(false);
        transportRepository.save(transport);
        log.info("Soft deleted transport with id: {}", id);
    }

    private TransportDto mapToDto(Transport t) {
        return TransportDto.builder()
                .id(t.getId())
                .partnerId(t.getPartner() != null ? t.getPartner().getId() : null)
                .partnerName(t.getPartner() != null ? t.getPartner().getName() : null)
                .transportType(t.getTransportType())
                .vehicleName(t.getVehicleName())
                .vehicleModel(t.getVehicleModel())
                .licensePlate(t.getLicensePlate())
                .capacity(t.getCapacity())
                .pickupLocation(t.getPickupLocation())
                .dropoffLocation(t.getDropoffLocation())
                .coverageArea(t.getCoverageArea())
                .pricePerDay(t.getPricePerDay())
                .pricePerTrip(t.getPricePerTrip())
                .totalUnits(t.getTotalUnits())
                .availableUnits(t.getAvailableUnits())
                .status(t.getStatus())
                .features(t.getFeatures())
                .active(t.isActive())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
