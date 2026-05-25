package com.travel.notification.dto;

import com.travel.notification.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BulkNotificationRequest {

    private List<String> userIds;

    @NotNull
    private NotificationType type;

    @NotBlank
    private String subject;

    @NotBlank
    private String message;

    private String referenceId;

    private String referenceType;
}
