package com.travel.billing.service;

import com.travel.billing.client.BookingClient;
import com.travel.billing.dto.InvoiceDto;
import com.travel.billing.dto.InvoiceRequest;
import com.travel.billing.entity.Invoice;
import com.travel.billing.exception.ResourceNotFoundException;
import com.travel.billing.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private BookingClient bookingClient;

    @InjectMocks
    private InvoiceService invoiceService;

    private InvoiceRequest invoiceRequest;
    private Invoice invoice;

    @BeforeEach
    void setUp() {
        invoiceRequest = new InvoiceRequest();
        invoiceRequest.setBookingId(1L);
        invoiceRequest.setUserId(1L);
        invoiceRequest.setSubtotal(new BigDecimal("1000.00"));
        invoiceRequest.setTaxAmount(new BigDecimal("100.00"));
        invoiceRequest.setDiscountAmount(new BigDecimal("50.00"));
        invoiceRequest.setCurrency("USD");
        invoiceRequest.setDueDate(LocalDate.now().plusDays(30));

        invoice = Invoice.builder()
                .id(1L)
                .invoiceNumber("INV-20240601-0001")
                .bookingId(1L)
                .userId(1L)
                .subtotal(new BigDecimal("1000.00"))
                .taxAmount(new BigDecimal("100.00"))
                .discountAmount(new BigDecimal("50.00"))
                .totalAmount(new BigDecimal("1050.00"))
                .currency("USD")
                .invoiceDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .status("DRAFT")
                .payments(new ArrayList<>())
                .build();
    }

    @Test
    void testCreateInvoice_VerifyTotalAmountCalculation() {
        // subtotal(1000) + tax(100) - discount(50) = 1050
        when(invoiceRepository.count()).thenReturn(0L);
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        InvoiceDto result = invoiceService.createInvoice(invoiceRequest);

        assertThat(result).isNotNull();
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("1050.00"));
        assertThat(result.getStatus()).isEqualTo("DRAFT");
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    void testCreateInvoice_WithNullTaxAndDiscount_DefaultsToZero() {
        invoiceRequest.setTaxAmount(null);
        invoiceRequest.setDiscountAmount(null);

        Invoice invoiceNoExtras = Invoice.builder()
                .id(2L)
                .invoiceNumber("INV-20240601-0002")
                .bookingId(1L)
                .userId(1L)
                .subtotal(new BigDecimal("1000.00"))
                .taxAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("1000.00"))
                .currency("USD")
                .invoiceDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .status("DRAFT")
                .payments(new ArrayList<>())
                .build();

        when(invoiceRepository.count()).thenReturn(1L);
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoiceNoExtras);

        InvoiceDto result = invoiceService.createInvoice(invoiceRequest);

        assertThat(result).isNotNull();
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    void testGetInvoiceById_NotFound_ThrowsException() {
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.getInvoiceById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Invoice");
    }

    @Test
    void testGetInvoiceById_Found_ReturnsDto() {
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        InvoiceDto result = invoiceService.getInvoiceById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getInvoiceNumber()).isEqualTo("INV-20240601-0001");
    }

    @Test
    void testGetOverdueInvoices_ReturnsListFromRepository() {
        Invoice overdueInvoice = Invoice.builder()
                .id(2L)
                .invoiceNumber("INV-20230101-0001")
                .bookingId(2L)
                .userId(2L)
                .subtotal(new BigDecimal("500.00"))
                .taxAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("500.00"))
                .currency("USD")
                .invoiceDate(LocalDate.now().minusMonths(2))
                .dueDate(LocalDate.now().minusDays(10))
                .status("SENT")
                .payments(new ArrayList<>())
                .build();

        when(invoiceRepository.findOverdueInvoices(any(LocalDate.class)))
                .thenReturn(Arrays.asList(overdueInvoice));

        List<InvoiceDto> result = invoiceService.getOverdueInvoices();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("SENT");
        verify(invoiceRepository, times(1)).findOverdueInvoices(any(LocalDate.class));
    }

    @Test
    void testGetOverdueInvoices_EmptyList() {
        when(invoiceRepository.findOverdueInvoices(any(LocalDate.class))).thenReturn(List.of());

        List<InvoiceDto> result = invoiceService.getOverdueInvoices();

        assertThat(result).isEmpty();
    }

    // -----------------------------------------------------------------------
    // POSITIVE: get invoice by invoice number returns correct DTO
    // -----------------------------------------------------------------------
    @Test
    void testGetInvoiceByNumber_Found_ReturnsDto() {
        when(invoiceRepository.findByInvoiceNumber("INV-20240601-0001"))
                .thenReturn(Optional.of(invoice));

        InvoiceDto result = invoiceService.getInvoiceByNumber("INV-20240601-0001");

        assertThat(result).isNotNull();
        assertThat(result.getInvoiceNumber()).isEqualTo("INV-20240601-0001");
    }

    // -----------------------------------------------------------------------
    // NEGATIVE: get invoice by number throws exception when not found
    // -----------------------------------------------------------------------
    @Test
    void testGetInvoiceByNumber_NotFound_ThrowsException() {
        when(invoiceRepository.findByInvoiceNumber("INV-UNKNOWN"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.getInvoiceByNumber("INV-UNKNOWN"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Invoice");
    }

    // -----------------------------------------------------------------------
    // POSITIVE: update invoice status from DRAFT to SENT
    // -----------------------------------------------------------------------
    @Test
    void testUpdateInvoiceStatus_DraftToSent_Success() {
        Invoice updatedInvoice = Invoice.builder()
                .id(1L)
                .invoiceNumber("INV-20240601-0001")
                .bookingId(1L)
                .userId(1L)
                .subtotal(new BigDecimal("1000.00"))
                .taxAmount(new BigDecimal("100.00"))
                .discountAmount(new BigDecimal("50.00"))
                .totalAmount(new BigDecimal("1050.00"))
                .currency("USD")
                .invoiceDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .status("SENT")
                .payments(new ArrayList<>())
                .build();

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(updatedInvoice);

        InvoiceDto result = invoiceService.updateInvoiceStatus(1L, "SENT");

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("SENT");
        verify(invoiceRepository).save(any(Invoice.class));
    }

    // -----------------------------------------------------------------------
    // NEGATIVE: update invoice status throws exception when invoice not found
    // -----------------------------------------------------------------------
    @Test
    void testUpdateInvoiceStatus_NotFound_ThrowsException() {
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.updateInvoiceStatus(99L, "SENT"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Invoice");
    }
}
