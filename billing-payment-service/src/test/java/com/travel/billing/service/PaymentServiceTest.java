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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private BookingClient bookingClient;

    @InjectMocks
    private PaymentService paymentService;

    private Invoice invoice;
    private Payment completedPayment;
    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        invoice = Invoice.builder()
                .id(1L)
                .invoiceNumber("INV-20240601-0001")
                .bookingId(10L)
                .userId(1L)
                .subtotal(new BigDecimal("1000.00"))
                .taxAmount(new BigDecimal("100.00"))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("1100.00"))
                .currency("USD")
                .invoiceDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .status("SENT")
                .payments(new ArrayList<>())
                .build();

        completedPayment = Payment.builder()
                .id(1L)
                .transactionId("TXN-" + UUID.randomUUID().toString().toUpperCase())
                .invoice(invoice)
                .userId(1L)
                .amount(new BigDecimal("1100.00"))
                .currency("USD")
                .paymentMethod("CREDIT_CARD")
                .status(PaymentStatus.COMPLETED)
                .paymentDate(LocalDateTime.now())
                .gatewayTransactionId("GW-" + UUID.randomUUID().toString().toUpperCase())
                .build();

        paymentRequest = new PaymentRequest();
        paymentRequest.setInvoiceId(1L);
        paymentRequest.setUserId(1L);
        paymentRequest.setAmount(new BigDecimal("1100.00"));
        paymentRequest.setCurrency("USD");
        paymentRequest.setPaymentMethod("CREDIT_CARD");
    }

    @Test
    void testProcessPayment_InvoiceNotFound_ThrowsException() {
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        paymentRequest.setInvoiceId(99L);

        assertThatThrownBy(() -> paymentService.processPayment(paymentRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Invoice");
    }

    @Test
    void testProcessPayment_AlreadyPaidInvoice_ThrowsException() {
        invoice.setStatus("PAID");
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> paymentService.processPayment(paymentRequest))
                .isInstanceOf(PaymentProcessingException.class)
                .hasMessageContaining("already paid");
    }

    @Test
    void testProcessPayment_InvoiceExists_PaymentSaved() {
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        Payment savedPayment = Payment.builder()
                .id(1L)
                .transactionId("TXN-ABC123")
                .invoice(invoice)
                .userId(1L)
                .amount(new BigDecimal("1100.00"))
                .currency("USD")
                .paymentMethod("CREDIT_CARD")
                .status(PaymentStatus.COMPLETED)
                .paymentDate(LocalDateTime.now())
                .gatewayTransactionId("GW-XYZ789")
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(paymentRepository.getTotalPaidForInvoice(anyLong())).thenReturn(new BigDecimal("100.00"));

        PaymentDto result = paymentService.processPayment(paymentRequest);

        assertThat(result).isNotNull();
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testRefundPayment_NotFound_ThrowsException() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setPaymentId(99L);
        refundRequest.setRefundAmount(new BigDecimal("100.00"));
        refundRequest.setReason("Customer requested");

        assertThatThrownBy(() -> paymentService.refundPayment(refundRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Payment");
    }

    @Test
    void testRefundPayment_NotCompleted_ThrowsException() {
        completedPayment.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(completedPayment));

        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setPaymentId(1L);
        refundRequest.setRefundAmount(new BigDecimal("100.00"));
        refundRequest.setReason("Customer requested");

        assertThatThrownBy(() -> paymentService.refundPayment(refundRequest))
                .isInstanceOf(PaymentProcessingException.class)
                .hasMessageContaining("completed payments");
    }

    @Test
    void testRefundPayment_ValidRefund_CreatesRefundPayment() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(completedPayment));

        Payment refundPaymentEntity = Payment.builder()
                .id(2L)
                .transactionId("TXN-REFUND-ABC")
                .invoice(invoice)
                .userId(1L)
                .amount(new BigDecimal("-500.00"))
                .currency("USD")
                .paymentMethod("CREDIT_CARD")
                .status(PaymentStatus.REFUNDED)
                .paymentDate(LocalDateTime.now())
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(refundPaymentEntity);

        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setPaymentId(1L);
        refundRequest.setRefundAmount(new BigDecimal("500.00"));
        refundRequest.setReason("Customer requested refund");

        PaymentDto result = paymentService.refundPayment(refundRequest);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testRefundPayment_AmountExceedsOriginal_ThrowsException() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(completedPayment));

        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setPaymentId(1L);
        refundRequest.setRefundAmount(new BigDecimal("9999.00")); // exceeds 1100
        refundRequest.setReason("Test");

        assertThatThrownBy(() -> paymentService.refundPayment(refundRequest))
                .isInstanceOf(PaymentProcessingException.class)
                .hasMessageContaining("cannot exceed");
    }
}
