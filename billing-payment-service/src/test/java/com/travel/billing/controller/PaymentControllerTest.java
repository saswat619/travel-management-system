package com.travel.billing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.billing.config.AuditConfig;
import com.travel.billing.dto.PaymentDto;
import com.travel.billing.dto.PaymentRequest;
import com.travel.billing.entity.PaymentStatus;
import com.travel.billing.security.JwtAuthenticationFilter;
import com.travel.billing.security.JwtUtil;
import com.travel.billing.service.InvoiceService;
import com.travel.billing.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {PaymentController.class, InvoiceController.class},
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuditConfig.class)
    })
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private InvoiceService invoiceService;

    @MockBean
    private JwtUtil jwtUtil;

    private PaymentDto paymentDto;

    @BeforeEach
    void setUp() {
        paymentDto = PaymentDto.builder()
                .id(1L)
                .transactionId("TXN-ABC12345")
                .invoiceId(1L)
                .userId(1L)
                .amount(new BigDecimal("1100.00"))
                .currency("USD")
                .paymentMethod("CREDIT_CARD")
                .status(PaymentStatus.COMPLETED)
                .paymentDate(LocalDateTime.now())
                .gatewayTransactionId("GW-XYZ789")
                .build();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testProcessPayment_Returns200() throws Exception {
        PaymentRequest request = new PaymentRequest();
        request.setInvoiceId(1L);
        request.setUserId(1L);
        request.setAmount(new BigDecimal("1100.00"));
        request.setCurrency("USD");
        request.setPaymentMethod("CREDIT_CARD");

        when(paymentService.processPayment(any(PaymentRequest.class))).thenReturn(paymentDto);

        mockMvc.perform(post("/api/billing/payments/process")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.transactionId").value("TXN-ABC12345"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetPaymentById_Returns200() throws Exception {
        when(paymentService.getPaymentById(1L)).thenReturn(paymentDto);

        mockMvc.perform(get("/api/billing/payments/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.transactionId").value("TXN-ABC12345"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testProcessPayment_ValidationFails_Returns400() throws Exception {
        PaymentRequest invalidRequest = new PaymentRequest();
        // Missing required fields

        mockMvc.perform(post("/api/billing/payments/process")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
