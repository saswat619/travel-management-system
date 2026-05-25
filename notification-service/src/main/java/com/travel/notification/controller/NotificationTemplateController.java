package com.travel.notification.controller;

import com.travel.notification.dto.NotificationTemplateDto;
import com.travel.notification.dto.TemplateRequest;
import com.travel.notification.service.NotificationTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notification/templates")
@RequiredArgsConstructor
@Tag(name = "Notification Templates", description = "Notification template management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class NotificationTemplateController {

    private final NotificationTemplateService notificationTemplateService;

    @GetMapping
    @Operation(summary = "Get all templates")
    public ResponseEntity<List<NotificationTemplateDto>> getAllTemplates() {
        return ResponseEntity.ok(notificationTemplateService.getAllTemplates());
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get template by code")
    public ResponseEntity<NotificationTemplateDto> getTemplateByCode(@PathVariable String code) {
        return ResponseEntity.ok(notificationTemplateService.getTemplateByCode(code));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create notification template")
    public ResponseEntity<NotificationTemplateDto> createTemplate(@Valid @RequestBody TemplateRequest request) {
        return ResponseEntity.ok(notificationTemplateService.createTemplate(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update notification template")
    public ResponseEntity<NotificationTemplateDto> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody TemplateRequest request) {
        return ResponseEntity.ok(notificationTemplateService.updateTemplate(id, request));
    }

    @PostMapping("/process")
    @Operation(summary = "Process template with variables")
    public ResponseEntity<String> processTemplate(
            @RequestParam String templateCode,
            @RequestBody Map<String, String> variables) {
        return ResponseEntity.ok(notificationTemplateService.processTemplate(templateCode, variables));
    }
}
