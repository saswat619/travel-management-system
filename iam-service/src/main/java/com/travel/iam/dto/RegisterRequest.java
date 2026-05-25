package com.travel.iam.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Invalid phone number")
    private String phone;

    // Valid values: ROLE_TRAVELER, ROLE_TRAVEL_AGENT, ROLE_CORPORATE_MANAGER,
    //               ROLE_FINANCE_OFFICER, ROLE_COMPLIANCE_OFFICER, ROLE_ADMIN
    @Builder.Default
    private String role = "ROLE_TRAVELER";
}
