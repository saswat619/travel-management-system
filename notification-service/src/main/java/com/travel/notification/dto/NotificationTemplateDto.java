package com.travel.notification.dto;

import com.travel.notification.enums.NotificationType;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateDto {
    private Long id;
    private String templateCode;
    private String name;
    private NotificationType type;
    private String subject;
    private String body;
    private boolean active;
}
