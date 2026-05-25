package com.travel.billing.service;

import com.travel.billing.client.BookingClient;
import com.travel.billing.dto.PaymentDto;
import com.travel.billing.dto.PaymentRequest;
import com.travel.billing.dto.RefundRequest;
import com.travel.billing.entity.Invoice;
import com.travel.billing.entity.Payment;
import com.travel.billing.entity.PaymentStatus;
import com.travel.billing.exception.PaymentProcessingException;
import com.travel.billing.exception.ResourceNotFoundException;
import com.travel.billing.repository.InvoiceRepository;
import com.travel.billing.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final BookingClient bookingClient;

    @Transactional(readOnly = true)
    public Page<PaymentDto> getAllPayments(Pageable pageable) {
        log.info("Fetching all payments");
        return paymentRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<PaymentDto> getPaymentsByUser(Long userId, Pageable pageable) {
        log.info("Fetching payments for user: {}", userId);
        return paymentRepository.findByUserId(userId, pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public PaymentDto getPaymentById(Long id) {
        log.info("Fetching payment with id: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        return mapToDto(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentDto> getPaymentsByInvoice(Long invoiceId) {
        log.info("Fetching payments for invoice: {}", invoiceId);
        return paymentRepository.findByInvoiceId(invoiceId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public PaymentDto processPayment(PaymentRequest req) {
        log.info("Processing payment for invoice: {}, amount: {}", req.getInvoiceId(), req.getAmount());

        Invoice invoice = invoiceRepository.findById(req.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", req.getInvoiceId()));

        if ("PAID".equals(invoice.getStatus())) {
            throw new PaymentProcessingException("Invoice " + invoice.getInvoiceNumber() + " is already paid.");
        }
        if ("CANCELLED".equals(invoice.getStatus())) {
            throw new PaymentProcessingException("Invoice " + invoice.getInvoiceNumber() + " is cancelled and cannot be paid.");
        }

        String transactionId = "TXN-" + UUID.randomUUID().toString().toUpperCase();

        // Simulate payment processing: 90% success rate
        boolean paymentSuccess = ThreadLocalRandom.current().nextInt(100) < 90;

        Payment payment = Payment.builder()
                .transactionId(transactionId)
                .invoice(invoice)
                .userId(req.getUserId())
                .amount(req.getAmount())
                .currency(req.getCurrency() != null ? req.getCurrency() : "USD")
                .paymentMethod(req.getPaymentMethod())
                .build();

        if (paymentSuccess) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setPaymentDate(LocalDateTime.now());
            payment.setGatewayTransactionId("GW-" + UUID.randomUUID().toString().toUpperCase());
            log.info("Payment successful. Transaction ID: {}", transactionId);

            Payment saved = paymentRepository.save(payment);

            // Check if invoice is fully paid
            BigDecimal totalPaid = paymentRepository.getTotalPaidForInvoice(invoice.getId());
            if (totalPaid == null) totalPaid = BigDecimal.ZERO;
            totalPaid = totalPaid.add(req.getAmount());

            if (totalPaid.compareTo(invoice.getTotalAmount()) >= 0) {
                invoice.setStatus("PAID");
                invoiceRepository.save(invoice);
                log.info("Invoice {} fully paid. Updating booking status.", invoice.getInvoiceNumber());

                // Notify booking service
                try {
                    Map<String, String> statusRequest = new HashMap<>();
                    statusRequest.put("status", "CONFIRMED");
                    bookingClient.updateBookingStatus(invoice.getBookingId(), statusRequest);
                    log.info("Booking {} status updated to CONFIRMED", invoice.getBookingId());
                } catch (Exception e) {
                    log.warn("Could not update booking status for bookingId: {}. Error: {}",
                            invoice.getBookingId(), e.getMessage());
                }
            }

            return mapToDto(saved);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Payment gateway declined the transaction. Please retry or use a different payment method.");
            log.warn("Payment failed for invoice: {}. Transaction ID: {}", req.getInvoiceId(), transactionId);

            Payment saved = paymentRepository.save(payment);
            return mapToDto(saved);
        }
    }

    public PaymentDto refundPayment(RefundRequest req) {
        log.info("Processing refund for payment: {}, amount: {}", req.getPaymentId(), req.getRefundAmount());

        Payment originalPayment = paymentRepository.findById(req.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", req.getPaymentId()));

        if (originalPayment.getStatus() != PaymentStatus.COMPLETED) {
            throw new PaymentProcessingException("Only completed payments can be refunded. Current status: "
                    + originalPayment.getStatus());
        }

        if (req.getRefundAmount().compareTo(originalPayment.getAmount()) > 0) {
            throw new PaymentProcessingException("Refund amount cannot exceed original payment amount of "
                    + originalPayment.getAmount());
        }

        String refundTransactionId = "TXN-REFUND-" + UUID.randomUUID().toString().toUpperCase();

        Payment refundPayment = Payment.builder()
                .transactionId(refundTransactionId)
                .invoice(originalPayment.getInvoice())
                .userId(originalPayment.getUserId())
                .amount(req.getRefundAmount().negate())
                .currency(originalPayment.getCurrency())
                .paymentMethod(originalPayment.getPaymentMethod())
                .status(PaymentStatus.REFUNDED)
                .paymentDate(LocalDateTime.now())
                .gatewayTransactionId("GW-REFUND-" + UUID.randomUUID().toString().toUpperCase())
                .failureReason(req.getReason())
                .build();

        Payment savedRefund = paymentRepository.save(refundPayment);

        // Update invoice status if needed
        Invoice invoice = originalPayment.getInvoice();
        if ("PAID".equals(invoice.getStatus())) {
            invoice.setStatus("SENT");
            invoiceRepository.save(invoice);
        }

        log.info("Refund processed. Refund Transaction ID: {}", refundTransactionId);
        return mapToDto(savedRefund);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalPaidForInvoice(Long invoiceId) {
        BigDecimal total = paymentRepository.getTotalPaidForInvoice(invoiceId);
        return total != null ? total : BigDecimal.ZERO;
    }

    private PaymentDto mapToDto(Payment payment) {
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
