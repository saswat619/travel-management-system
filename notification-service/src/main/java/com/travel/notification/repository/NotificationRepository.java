package com.travel.notification.repository;

import com.travel.notification.entity.Notification;
import com.travel.notification.enums.NotificationStatus;
import com.travel.notification.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByNotificationId(String notificationId);

    Page<Notification> findByUserId(String userId, Pageable pageable);

    Page<Notification> findByUserIdAndStatus(String userId, NotificationStatus status, Pageable pageable);

    Page<Notification> findByStatus(NotificationStatus status, Pageable pageable);

    Page<Notification> findByType(NotificationType type, Pageable pageable);

    Long countByUserIdAndStatus(String userId, NotificationStatus status);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.status = 'PENDING' ORDER BY n.createdAt DESC")
    List<Notification> findPendingByUser(@Param("userId") String userId);
}
