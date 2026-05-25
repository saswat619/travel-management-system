package com.travel.iam.service;

import com.travel.iam.dto.AuthResponse;
import com.travel.iam.dto.LoginRequest;
import com.travel.iam.dto.RegisterRequest;
import com.travel.iam.entity.Role;
import com.travel.iam.entity.User;
import com.travel.iam.repository.RoleRepository;
import com.travel.iam.repository.UserRepository;
import com.travel.iam.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    // Valid Travel360 roles
    private static final List<String> VALID_ROLES = List.of(
            "ROLE_TRAVELER",
            "ROLE_TRAVEL_AGENT",
            "ROLE_CORPORATE_MANAGER",
            "ROLE_FINANCE_OFFICER",
            "ROLE_COMPLIANCE_OFFICER",
            "ROLE_ADMIN"
    );

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        log.info("Registering new user: {}", req.getUsername());

        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Username already exists: " + req.getUsername());
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already registered: " + req.getEmail());
        }

        // Validate role
        String roleName = (req.getRole() != null && VALID_ROLES.contains(req.getRole()))
                ? req.getRole()
                : "ROLE_TRAVELER";

        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name(roleName).build()
                ));

        User user = User.builder()
                .username(req.getUsername())
                .fullName(req.getFullName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .roles(Set.of(role))
                .enabled(true)
                .build();

        userRepository.save(user);
        log.info("User registered successfully: {}", req.getUsername());

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );

        String token = jwtTokenProvider.generateToken(auth);
        List<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .username(user.getUsername())
                .roles(roles)
                .build();
    }

    public AuthResponse login(LoginRequest req) {
        log.info("Login attempt for user: {}", req.getUsername());

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );

        String token = jwtTokenProvider.generateToken(auth);

        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());

        log.info("User logged in successfully: {}", req.getUsername());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .username(user.getUsername())
                .roles(roles)
                .build();
    }
}
