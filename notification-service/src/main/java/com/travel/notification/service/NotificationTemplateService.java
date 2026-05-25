package com.travel.notification.service;

import com.travel.notification.dto.NotificationTemplateDto;
import com.travel.notification.dto.TemplateRequest;
import com.travel.notification.entity.NotificationTemplate;
import com.travel.notification.exception.ResourceNotFoundException;
import com.travel.notification.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationTemplateService {

    private final NotificationTemplateRepository notificationTemplateRepository;

    public List<NotificationTemplateDto> getAllTemplates() {
        return notificationTemplateRepository.findAll()
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public NotificationTemplateDto getTemplateByCode(String code) {
        NotificationTemplate template = notificationTemplateRepository.findByTemplateCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with code: " + code));
        return mapToDto(template);
    }

    public NotificationTemplateDto createTemplate(TemplateRequest req) {
        NotificationTemplate template = NotificationTemplate.builder()
                .templateCode(req.getTemplateCode())
                .name(req.getName())
                .type(req.getType())
                .subject(req.getSubject())
                .body(req.getBody())
                .active(true)
                .build();
        return mapToDto(notificationTemplateRepository.save(template));
    }

    public NotificationTemplateDto updateTemplate(Long id, TemplateRequest req) {
        NotificationTemplate template = notificationTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));
        template.setTemplateCode(req.getTemplateCode());
        template.setName(req.getName());
        template.setType(req.getType());
        template.setSubject(req.getSubject());
        template.setBody(req.getBody());
        return mapToDto(notificationTemplateRepository.save(template));
    }

    public String processTemplate(String templateCode, Map<String, String> variables) {
        NotificationTemplate template = notificationTemplateRepository.findByTemplateCode(templateCode)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with code: " + templateCode));

        String body = template.getBody();
        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                body = body.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return body;
    }

    private NotificationTemplateDto mapToDto(NotificationTemplate t) {
        return NotificationTemplateDto.builder()
                .id(t.getId())
                .templateCode(t.getTemplateCode())
                .name(t.getName())
                .type(t.getType())
                .subject(t.getSubject())
                .body(t.getBody())
                .active(t.isActive())
                .build();
    }
}
