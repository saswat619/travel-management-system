package com.travel.notification.repository;

import com.travel.notification.entity.NotificationTemplate;
import com.travel.notification.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    Optional<NotificationTemplate> findByTemplateCode(String code);

    List<NotificationTemplate> findByTypeAndActiveTrue(NotificationType type);
}
