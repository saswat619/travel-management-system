package com.travel.notification.dto;

import com.travel.notification.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TemplateRequest {

    @NotBlank
    private String templateCode;

    @NotBlank
    private String name;

    @NotNull
    private NotificationType type;

    @NotBlank
    private String subject;

    @NotBlank
    private String body;
}
