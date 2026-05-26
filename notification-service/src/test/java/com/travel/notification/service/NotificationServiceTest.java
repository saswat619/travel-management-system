package com.travel.notification.service;

import com.travel.notification.dto.BulkNotificationRequest;
import com.travel.notification.dto.NotificationDto;
import com.travel.notification.dto.NotificationRequest;
import com.travel.notification.entity.Notification;
import com.travel.notification.enums.NotificationStatus;
import com.travel.notification.enums.NotificationType;
import com.travel.notification.exception.ResourceNotFoundException;
import com.travel.notification.repository.NotificationRepository;
import com.travel.notification.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

    // -----------------------------------------------------------------------
    // POSITIVE: get notification by ID returns correct DTO
    // -----------------------------------------------------------------------
    @Test
    void testGetNotificationById_Found_ReturnsDto() {
        Notification notification = Notification.builder()
                .id(1L)
                .notificationId("NOTIF-ABCD1234")
                .userId("user123")
                .type(NotificationType.EMAIL)
                .subject("Welcome")
                .message("Hello!")
                .status(NotificationStatus.SENT)
                .retryCount(0)
                .build();

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        NotificationDto result = notificationService.getNotificationById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNotificationId()).isEqualTo("NOTIF-ABCD1234");
        assertThat(result.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    // -----------------------------------------------------------------------
    // NEGATIVE: get notification by ID throws exception when not found
    // -----------------------------------------------------------------------
    @Test
    void testGetNotificationById_NotFound_ThrowsException() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getNotificationById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // -----------------------------------------------------------------------
    // NEGATIVE: mark as read throws exception when notification not found
    // -----------------------------------------------------------------------
    @Test
    void testMarkAsRead_NotFound_ThrowsException() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // -----------------------------------------------------------------------
    // POSITIVE: retry failed notification updates status to SENT
    // -----------------------------------------------------------------------
    @Test
    void testRetryFailedNotification_Success() {
        Notification failedNotification = Notification.builder()
                .id(1L)
                .notificationId("NOTIF-FAIL0001")
                .userId("user123")
                .userEmail("user@test.com")
                .type(NotificationType.EMAIL)
                .status(NotificationStatus.FAILED)
                .retryCount(1)
                .errorMessage("SMTP error")
                .build();

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(failedNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(failedNotification);

        notificationService.retryFailedNotification(1L);

        assertThat(failedNotification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(failedNotification.getRetryCount()).isEqualTo(2);
        verify(notificationRepository).save(failedNotification);
    }

    // -----------------------------------------------------------------------
    // NEGATIVE: retry non-failed notification throws IllegalStateException
    // -----------------------------------------------------------------------
    @Test
    void testRetryFailedNotification_NotFailed_ThrowsIllegalStateException() {
        Notification sentNotification = Notification.builder()
                .id(1L)
                .notificationId("NOTIF-SENT0001")
                .userId("user123")
                .status(NotificationStatus.SENT)
                .retryCount(0)
                .build();

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(sentNotification));

        assertThatThrownBy(() -> notificationService.retryFailedNotification(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not in FAILED status");
    }

    // -----------------------------------------------------------------------
    // NEGATIVE: retry notification throws exception when not found
    // -----------------------------------------------------------------------
    @Test
    void testRetryFailedNotification_NotFound_ThrowsException() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.retryFailedNotification(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // -----------------------------------------------------------------------
    // POSITIVE: send bulk notification sends one notification per userId
    // -----------------------------------------------------------------------
    @Test
    void testSendBulkNotification_SendsNotificationForEachUser() {
        BulkNotificationRequest bulkRequest = new BulkNotificationRequest();
        bulkRequest.setUserIds(List.of("user1", "user2", "user3"));
        bulkRequest.setType(NotificationType.IN_APP);
        bulkRequest.setSubject("System Maintenance");
        bulkRequest.setMessage("Scheduled downtime at midnight.");

        Notification n1 = Notification.builder().id(1L).notificationId("NOTIF-001")
                .userId("user1").status(NotificationStatus.SENT).retryCount(0).build();
        Notification n2 = Notification.builder().id(2L).notificationId("NOTIF-002")
                .userId("user2").status(NotificationStatus.SENT).retryCount(0).build();
        Notification n3 = Notification.builder().id(3L).notificationId("NOTIF-003")
                .userId("user3").status(NotificationStatus.SENT).retryCount(0).build();

        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(n1).thenReturn(n2).thenReturn(n3);

        List<NotificationDto> results = notificationService.sendBulkNotification(bulkRequest);

        assertThat(results).hasSize(3);
        verify(notificationRepository, times(3)).save(any(Notification.class));
    }

    // -----------------------------------------------------------------------
    // POSITIVE: unread count returns 0 when no unread notifications
    // -----------------------------------------------------------------------
    @Test
    void testGetUnreadCount_ReturnsZeroWhenNoUnread() {
        String userId = "user999";
        when(notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.PENDING)).thenReturn(0L);
        when(notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.SENT)).thenReturn(0L);

        Long count = notificationService.getUnreadCount(userId);

        assertThat(count).isEqualTo(0L);
    }
}
