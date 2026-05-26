package com.travel.iam.service;

import com.travel.iam.dto.AuthResponse;
import com.travel.iam.dto.LoginRequest;
import com.travel.iam.dto.RegisterRequest;
import com.travel.iam.entity.Role;
import com.travel.iam.entity.User;
import com.travel.iam.repository.RoleRepository;
import com.travel.iam.repository.UserRepository;
import com.travel.iam.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerSuccess() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .role("ROLE_ADMIN")  // ROLE_ADMIN is in VALID_ROLES
                .build();

        Role role = Role.builder().id(1L).name("ROLE_ADMIN").build();

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User savedUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .roles(roles)
                .build();

        Authentication mockAuth = mock(Authentication.class);

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("mock-jwt-token");

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getAccessToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("Bearer", response.getTokenType());
        assertTrue(response.getRoles().contains("ROLE_ADMIN"));

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerFailsWhenUsernameExists() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("existinguser")
                .email("test@example.com")
                .password("password123")
                .build();

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.register(request));

        assertTrue(exception.getMessage().contains("Username already exists"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginSuccess() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        Role role = Role.builder().id(1L).name("ROLE_ADMIN").build();
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .roles(roles)
                .build();

        Authentication mockAuth = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("mock-login-token");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("mock-login-token", response.getAccessToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("Bearer", response.getTokenType());
        assertTrue(response.getRoles().contains("ROLE_ADMIN"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateToken(any(Authentication.class));
        verify(userRepository).findByUsername("testuser");
    }

    // -----------------------------------------------------------------------
    // NEGATIVE: register fails when email is already registered
    // -----------------------------------------------------------------------
    @Test
    void registerFailsWhenEmailExists() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("existing@example.com")
                .password("password123")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.register(request));

        assertTrue(exception.getMessage().contains("Email already registered"));
        verify(userRepository, never()).save(any(User.class));
    }

    // -----------------------------------------------------------------------
    // POSITIVE (edge case): invalid role defaults to ROLE_TRAVELER
    // -----------------------------------------------------------------------
    @Test
    void registerWithInvalidRole_DefaultsToTraveler() {
        RegisterRequest request = RegisterRequest.builder()
                .username("traveleruser")
                .email("traveler@example.com")
                .password("password123")
                .role("ROLE_HACKER")          // not in VALID_ROLES
                .build();

        Role travelerRole = Role.builder().id(2L).name("ROLE_TRAVELER").build();
        Set<Role> roles = new HashSet<>();
        roles.add(travelerRole);

        User savedUser = User.builder()
                .id(2L)
                .username("traveleruser")
                .email("traveler@example.com")
                .password("encodedPassword")
                .roles(roles)
                .build();

        Authentication mockAuth = mock(Authentication.class);

        when(userRepository.existsByUsername("traveleruser")).thenReturn(false);
        when(userRepository.existsByEmail("traveler@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_TRAVELER")).thenReturn(Optional.of(travelerRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(jwtTokenProvider.generateToken(mockAuth)).thenReturn("mock-traveler-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertTrue(response.getRoles().contains("ROLE_TRAVELER"));
        verify(roleRepository).findByName("ROLE_TRAVELER");
    }

    // -----------------------------------------------------------------------
    // NEGATIVE: login fails when user not found in DB after authentication
    // -----------------------------------------------------------------------
    @Test
    void loginFails_UserNotFoundAfterAuth() {
        LoginRequest request = new LoginRequest();
        request.setUsername("ghostuser");
        request.setPassword("password123");

        Authentication mockAuth = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("token");
        when(userRepository.findByUsername("ghostuser")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertTrue(exception.getMessage().contains("User not found"));
    }

    // -----------------------------------------------------------------------
    // NEGATIVE: login fails when authentication manager throws exception
    // -----------------------------------------------------------------------
    @Test
    void loginFails_BadCredentials() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertTrue(exception.getMessage().contains("Bad credentials"));
        verify(userRepository, never()).findByUsername(anyString());
    }
}
