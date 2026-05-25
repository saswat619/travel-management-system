package com.travel.iam.controller;

import com.travel.iam.dto.UserDto;
import com.travel.iam.security.JwtTokenProvider;
import com.travel.iam.security.UserDetailsServiceImpl;
import com.travel.iam.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private UserDto buildSampleUser() {
        return UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .enabled(true)
                .roles(Set.of("ROLE_USER"))
                .createdAt(LocalDateTime.now())
                .createdBy("system")
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsersReturns200() throws Exception {
        UserDto userDto = buildSampleUser();
        Page<UserDto> userPage = new PageImpl<>(List.of(userDto));

        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

        mockMvc.perform(get("/api/iam/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].username").value("testuser"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserByIdReturns200WithEntityModel() throws Exception {
        UserDto userDto = buildSampleUser();

        when(userService.getUserById(1L)).thenReturn(userDto);

        mockMvc.perform(get("/api/iam/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$._links.self").exists());
    }
}
