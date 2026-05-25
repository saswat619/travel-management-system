package com.travel.billing.service;

import com.travel.billing.client.BookingClient;
import com.travel.billing.dto.InvoiceDto;
import com.travel.billing.dto.InvoiceRequest;
import com.travel.billing.dto.PaymentDto;
import com.travel.billing.entity.Invoice;
import com.travel.billing.entity.Payment;
import com.travel.billing.exception.ResourceNotFoundException;
import com.travel.billing.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final BookingClient bookingClient;

    @Transactional(readOnly = true)
    public Page<InvoiceDto> getAllInvoices(Pageable pageable) {
        log.info("Fetching all invoices");
        return invoiceRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceDto> getInvoicesByUser(Long userId, Pageable pageable) {
        log.info("Fetching invoices for user: {}", userId);
        return invoiceRepository.findByUserId(userId, pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public InvoiceDto getInvoiceById(Long id) {
        log.info("Fetching invoice with id: {}", id);
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
        return mapToDto(invoice);
    }

    @Transactional(readOnly = true)
    public InvoiceDto getInvoiceByNumber(String invoiceNumber) {
        log.info("Fetching invoice with number: {}", invoiceNumber);
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "invoiceNumber", invoiceNumber));
        return mapToDto(invoice);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceDto> getInvoicesByBooking(Long bookingId, Pageable pageable) {
        log.info("Fetching invoices for booking: {}", bookingId);
        return invoiceRepository.findByBookingId(bookingId, pageable).map(this::mapToDto);
    }

    public InvoiceDto createInvoice(InvoiceRequest req) {
        log.info("Creating invoice for booking: {}, user: {}", req.getBookingId(), req.getUserId());

        BigDecimal taxAmount = req.getTaxAmount() != null ? req.getTaxAmount() : BigDecimal.ZERO;
        BigDecimal discountAmount = req.getDiscountAmount() != null ? req.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal totalAmount = req.getSubtotal().add(taxAmount).subtract(discountAmount);

        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber())
                .bookingId(req.getBookingId())
                .userId(req.getUserId())
                .subtotal(req.getSubtotal())
                .taxAmount(taxAmount)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .currency(req.getCurrency() != null ? req.getCurrency() : "USD")
                .invoiceDate(LocalDate.now())
                .dueDate(req.getDueDate())
                .status("DRAFT")
                .notes(req.getNotes())
                .build();

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Invoice created with number: {}", saved.getInvoiceNumber());
        return mapToDto(saved);
    }

    public InvoiceDto updateInvoiceStatus(Long id, String status) {
        log.info("Updating invoice {} status to {}", id, status);
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
        invoice.setStatus(status);
        Invoice saved = invoiceRepository.save(invoice);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<InvoiceDto> getOverdueInvoices() {
        log.info("Fetching overdue invoices");
        return invoiceRepository.findOverdueInvoices(LocalDate.now())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private String generateInvoiceNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = invoiceRepository.count() + 1;
        return String.format("INV-%s-%04d", datePart, count);
    }

    private InvoiceDto mapToDto(Invoice invoice) {
        List<PaymentDto> paymentDtos = invoice.getPayments().stream()
                .map(this::mapPaymentToDto)
                .collect(Collectors.toList());

        return InvoiceDto.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .bookingId(invoice.getBookingId())
                .userId(invoice.getUserId())
                .subtotal(invoice.getSubtotal())
                .taxAmount(invoice.getTaxAmount())
                .discountAmount(invoice.getDiscountAmount())
                .totalAmount(invoice.getTotalAmount())
                .currency(invoice.getCurrency())
                .invoiceDate(invoice.getInvoiceDate())
                .dueDate(invoice.getDueDate())
                .status(invoice.getStatus())
                .notes(invoice.getNotes())
                .payments(paymentDtos)
                .createdAt(invoice.getCreatedAt())
                .build();
    }

    private PaymentDto mapPaymentToDto(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .transactionId(payment.getTransactionId())
                .invoiceId(payment.getInvoice() != null ? payment.getInvoice().getId() : null)
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .paymentDate(payment.getPaymentDate())
                .gatewayTransactionId(payment.getGatewayTransactionId())
                .failureReason(payment.getFailureReason())
                .build();
    }
}
