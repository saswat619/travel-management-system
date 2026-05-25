package com.travel.notification.dto;

import com.travel.notification.enums.NotificationStatus;
import com.travel.notification.enums.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private String notificationId;
    private String userId;
    private String userEmail;
    private NotificationType type;
    private String subject;
    private String message;
    private NotificationStatus status;
    private String referenceId;
    private String referenceType;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private Integer retryCount;
    private LocalDateTime createdAt;
}
