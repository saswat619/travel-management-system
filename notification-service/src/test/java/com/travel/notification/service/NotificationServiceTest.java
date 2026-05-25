package com.travel.notification.service;

import com.travel.notification.dto.NotificationDto;
import com.travel.notification.dto.NotificationRequest;
import com.travel.notification.entity.Notification;
import com.travel.notification.enums.NotificationStatus;
import com.travel.notification.enums.NotificationType;
import com.travel.notification.repository.NotificationRepository;
import com.travel.notification.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationTemplateRepository notificationTemplateRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void testSendNotification_VerifyStatusSentAndSentAtSet() {
        NotificationRequest request = new NotificationRequest();
        request.setUserId("user123");
        request.setUserEmail("user@test.com");
        request.setType(NotificationType.EMAIL);
        request.setSubject("Test Subject");
        request.setMessage("Test Message");

        Notification savedNotification = Notification.builder()
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

        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        NotificationDto result = notificationService.sendNotification(request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(result.getSentAt()).isNotNull();
        assertThat(result.getUserId()).isEqualTo("user123");
    }

    @Test
    void testMarkAsRead_VerifyStatusReadAndReadAtSet() {
        Notification notification = Notification.builder()
                .id(1L)
                .notificationId("NOTIF-ABCD1234")
                .userId("user123")
                .status(NotificationStatus.SENT)
                .retryCount(0)
                .build();

        Notification readNotification = Notification.builder()
                .id(1L)
                .notificationId("NOTIF-ABCD1234")
                .userId("user123")
                .status(NotificationStatus.READ)
                .readAt(LocalDateTime.now())
                .retryCount(0)
                .build();

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(readNotification);

        NotificationDto result = notificationService.markAsRead(1L);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(NotificationStatus.READ);
        assertThat(result.getReadAt()).isNotNull();
    }

    @Test
    void testGetUnreadCount_ReturnsCorrectCount() {
        String userId = "user123";
        when(notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.PENDING)).thenReturn(3L);
        when(notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.SENT)).thenReturn(5L);

        Long count = notificationService.getUnreadCount(userId);

        assertThat(count).isEqualTo(8L);
    }
}
