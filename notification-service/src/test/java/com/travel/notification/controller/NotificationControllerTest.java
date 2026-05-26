package com.travel.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.notification.config.AuditConfig;
import com.travel.notification.dto.NotificationDto;
import com.travel.notification.dto.NotificationRequest;
import com.travel.notification.enums.NotificationStatus;
import com.travel.notification.enums.NotificationType;
import com.travel.notification.security.JwtAuthenticationFilter;
import com.travel.notification.security.JwtUtil;
import com.travel.notification.service.NotificationService;
import com.travel.notification.service.NotificationTemplateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = NotificationController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuditConfig.class)
        })
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationTemplateService notificationTemplateService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @WithMockUser
    void testSendNotification_Returns201() throws Exception {
        NotificationRequest request = new NotificationRequest();
        request.setUserId("user123");
        request.setUserEmail("user@test.com");
        request.setType(NotificationType.EMAIL);
        request.setSubject("Test Subject");
        request.setMessage("Test Message");

        NotificationDto responseDto = NotificationDto.builder()
                .id(1L)
                .notificationId("NOTIF-ABCD1234")
                .userId("user123")
                .userEmail("user@test.com")
                .type(NotificationType.EMAIL)
                .subject("Test Subject")
                .message("Test Message")
                .status(NotificationStatus.SENT)
                .sentAt(LocalDateTime.now())
                .retryCount(0)
                .build();

        when(notificationService.sendNotification(any(NotificationRequest.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/notification/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.notificationId").value("NOTIF-ABCD1234"))
                .andExpect(jsonPath("$.status").value("SENT"));
    }

    @Test
    @WithMockUser
    void testGetNotificationById_Returns200WithLinks() throws Exception {
        NotificationDto dto = NotificationDto.builder()
                .id(1L)
                .notificationId("NOTIF-ABCD1234")
                .userId("user123")
                .type(NotificationType.EMAIL)
                .subject("Test")
                .message("Test message")
                .status(NotificationStatus.SENT)
                .retryCount(0)
                .build();

        when(notificationService.getNotificationById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/notification/notifications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId").value("NOTIF-ABCD1234"))
                .andExpect(jsonPath("$.status").value("SENT"));
    }

    @Test
    @WithMockUser
    void testMarkAsRead_Returns200() throws Exception {
        NotificationDto dto = NotificationDto.builder()
                .id(1L)
                .notificationId("NOTIF-ABCD1234")
                .userId("user123")
                .status(NotificationStatus.READ)
                .readAt(LocalDateTime.now())
                .retryCount(0)
                .build();

        when(notificationService.markAsRead(1L)).thenReturn(dto);

        mockMvc.perform(patch("/api/notification/notifications/1/read")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READ"));
    }
}
