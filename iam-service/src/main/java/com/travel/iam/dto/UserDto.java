package com.travel.iam.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private boolean enabled;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private String createdBy;
}
