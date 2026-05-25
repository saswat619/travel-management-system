package com.travel.notification.service;

import com.travel.notification.dto.BulkNotificationRequest;
import com.travel.notification.dto.NotificationDto;
import com.travel.notification.dto.NotificationRequest;
import com.travel.notification.entity.Notification;
import com.travel.notification.enums.NotificationStatus;
import com.travel.notification.exception.ResourceNotFoundException;
import com.travel.notification.repository.NotificationRepository;
import com.travel.notification.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository notificationTemplateRepository;

    public NotificationDto sendNotification(NotificationRequest req) {
        String notificationId = "NOTIF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Notification notification = Notification.builder()
                .notificationId(notificationId)
                .userId(req.getUserId())
                .userEmail(req.getUserEmail())
                .type(req.getType())
                .subject(req.getSubject())
                .message(req.getMessage())
                .status(NotificationStatus.PENDING)
                .referenceId(req.getReferenceId())
                .referenceType(req.getReferenceType())
                .retryCount(0)
                .build();

        try {
            log.info("Sending {} notification to {}", req.getType(), req.getUserEmail());
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
        }

        return mapToDto(notificationRepository.save(notification));
    }

    public List<NotificationDto> sendBulkNotification(BulkNotificationRequest req) {
        List<NotificationDto> results = new ArrayList<>();
        if (req.getUserIds() != null) {
            for (String userId : req.getUserIds()) {
                NotificationRequest individualReq = new NotificationRequest();
                individualReq.setUserId(userId);
                individualReq.setType(req.getType());
                individualReq.setSubject(req.getSubject());
                individualReq.setMessage(req.getMessage());
                individualReq.setReferenceId(req.getReferenceId());
                individualReq.setReferenceType(req.getReferenceType());
                results.add(sendNotification(individualReq));
            }
        }
        return results;
    }

    public Page<NotificationDto> getAllNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable).map(this::mapToDto);
    }

    public Page<NotificationDto> getNotificationsByUser(String userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId, pageable).map(this::mapToDto);
    }

    public NotificationDto getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        return mapToDto(notification);
    }

    public NotificationDto getNotificationByNotificationId(String notifId) {
        Notification notification = notificationRepository.findByNotificationId(notifId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with notificationId: " + notifId));
        return mapToDto(notification);
    }

    public NotificationDto markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(LocalDateTime.now());
        return mapToDto(notificationRepository.save(notification));
    }

    public Long getUnreadCount(String userId) {
        Long pendingCount = notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.PENDING);
        Long sentCount = notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.SENT);
        return (pendingCount != null ? pendingCount : 0L) + (sentCount != null ? sentCount : 0L);
    }

    public Page<NotificationDto> getPendingNotifications(Pageable pageable) {
        return notificationRepository.findByStatus(NotificationStatus.PENDING, pageable).map(this::mapToDto);
    }

    public void retryFailedNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        if (notification.getStatus() != NotificationStatus.FAILED) {
            throw new IllegalStateException("Notification is not in FAILED status");
        }

        notification.setRetryCount(notification.getRetryCount() + 1);

        try {
            log.info("Retrying {} notification to {}", notification.getType(), notification.getUserEmail());
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notification.setErrorMessage(null);
        } catch (Exception e) {
            log.error("Retry failed for notification {}: {}", id, e.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
        }

        notificationRepository.save(notification);
    }

    private NotificationDto mapToDto(Notification n) {
        return NotificationDto.builder()
                .id(n.getId())
                .notificationId(n.getNotificationId())
                .userId(n.getUserId())
                .userEmail(n.getUserEmail())
                .type(n.getType())
                .subject(n.getSubject())
                .message(n.getMessage())
                .status(n.getStatus())
                .referenceId(n.getReferenceId())
                .referenceType(n.getReferenceType())
                .sentAt(n.getSentAt())
                .readAt(n.getReadAt())
                .retryCount(n.getRetryCount())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
