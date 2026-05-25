package com.travel.partner.service;

import com.travel.partner.dto.PartnerDto;
import com.travel.partner.dto.PartnerRequest;
import com.travel.partner.entity.Partner;
import com.travel.partner.exception.ResourceNotFoundException;
import com.travel.partner.repository.PartnerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartnerServiceTest {

    @Mock
    private PartnerRepository partnerRepository;

    @InjectMocks
    private PartnerService partnerService;

    @Test
    void createPartner_success() {
        // Given
        PartnerRequest request = new PartnerRequest();
        request.setName("Test Partner");
        request.setType("HOTEL");
        request.setContactEmail("test@example.com");
        request.setContactPhone("1234567890");
        request.setAddress("123 Test St");
        request.setWebsite("https://test.com");

        Partner savedPartner = Partner.builder()
                .id(1L)
                .name("Test Partner")
                .type("HOTEL")
                .contactEmail("test@example.com")
                .contactPhone("1234567890")
                .address("123 Test St")
                .website("https://test.com")
                .active(true)
                .build();

        when(partnerRepository.save(any(Partner.class))).thenReturn(savedPartner);

        // When
        PartnerDto result = partnerService.createPartner(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Partner");
        assertThat(result.getType()).isEqualTo("HOTEL");
        assertThat(result.isActive()).isTrue();
        verify(partnerRepository, times(1)).save(any(Partner.class));
    }

    @Test
    void getPartnerById_notFound_throwsException() {
        // Given
        when(partnerRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> partnerService.getPartnerById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Partner");

        verify(partnerRepository, times(1)).findById(999L);
    }

    @Test
    void getAllPartners_returnsPage() {
        // Given
        Partner partner1 = Partner.builder().id(1L).name("Partner A").type("HOTEL").active(true).build();
        Partner partner2 = Partner.builder().id(2L).name("Partner B").type("AIRLINE").active(true).build();
        Page<Partner> partnerPage = new PageImpl<>(List.of(partner1, partner2));

        Pageable pageable = PageRequest.of(0, 10);
        when(partnerRepository.findAll(pageable)).thenReturn(partnerPage);

        // When
        Page<PartnerDto> result = partnerService.getAllPartners(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Partner A");
        assertThat(result.getContent().get(1).getName()).isEqualTo("Partner B");
        verify(partnerRepository, times(1)).findAll(pageable);
    }
}
