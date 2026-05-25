package com.travel.notification.entity;

import com.travel.notification.enums.NotificationStatus;
import com.travel.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String notificationId;

    private String userId;

    private String userEmail;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    private String referenceId;

    private String referenceType;

    private LocalDateTime sentAt;

    private LocalDateTime readAt;

    @Builder.Default
    private Integer retryCount = 0;

    private String errorMessage;
}
