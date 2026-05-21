package com.blog.controller;

import com.blog.common.BusinessException;
import com.blog.common.GlobalExceptionHandler;
import com.blog.common.Result;
import com.blog.dto.UserDTO;
import com.blog.dto.UserVO;
import com.blog.service.UserService;
import com.blog.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;
    @Mock
    private JwtUtils jwtUtils;
    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ========== POST /api/user/register ==========

    @Test
    void register_success() throws Exception {
        UserDTO dto = new UserDTO();
        dto.setUsername("newuser");
        dto.setPassword("pass123");

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).register(any(UserDTO.class));
    }

    @Test
    void register_duplicateUsername_returns400() throws Exception {
        doThrow(new BusinessException(Result.BAD_REQUEST, "用户名已存在"))
                .when(userService).register(any(UserDTO.class));

        UserDTO dto = new UserDTO();
        dto.setUsername("existing");
        dto.setPassword("pass123");

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }

    // ========== POST /api/user/login ==========

    @Test
    void login_success() throws Exception {
        when(userService.login("admin", "secret")).thenReturn(
                Map.of("accessToken", "at", "refreshToken", "rt"));

        String body = new ObjectMapper().writeValueAsString(
                Map.of("username", "admin", "password", "secret"));

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("at"))
                .andExpect(jsonPath("$.data.refreshToken").value("rt"));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        when(userService.login("admin", "wrong"))
                .thenThrow(new BusinessException(Result.UNAUTHORIZED, "用户名或密码错误"));

        String body = new ObjectMapper().writeValueAsString(
                Map.of("username", "admin", "password", "wrong"));

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    // ========== POST /api/user/refresh ==========

    @Test
    void refreshToken_success() throws Exception {
        when(jwtUtils.validateRefreshToken("valid-rt")).thenReturn(1L);
        when(jwtUtils.getUsername("valid-rt")).thenReturn("admin");
        when(jwtUtils.generateAccessToken(1L, "admin")).thenReturn("new-at");

        String body = new ObjectMapper().writeValueAsString(
                Map.of("refreshToken", "valid-rt"));

        mockMvc.perform(post("/api/user/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-at"));
    }

    @Test
    void refreshToken_empty_returns400() throws Exception {
        String body = new ObjectMapper().writeValueAsString(
                Map.of("refreshToken", ""));

        mockMvc.perform(post("/api/user/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Result.BAD_REQUEST))
                .andExpect(jsonPath("$.message").value("RefreshToken 不能为空"));
    }

    @Test
    void refreshToken_invalid_returns401() throws Exception {
        when(jwtUtils.validateRefreshToken("bad-rt"))
                .thenThrow(new RuntimeException("invalid"));

        String body = new ObjectMapper().writeValueAsString(
                Map.of("refreshToken", "bad-rt"));

        mockMvc.perform(post("/api/user/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Result.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("RefreshToken 无效或已过期"));
    }

    // ========== GET /api/user/info ==========

    @Test
    void getCurrentUserInfo_success() throws Exception {
        UserVO vo = new UserVO();
        vo.setId(1L);
        vo.setUsername("admin");

        when(userService.getUserInfo(1L)).thenReturn(vo);

        mockMvc.perform(get("/api/user/info")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    void getCurrentUserInfo_notFound() throws Exception {
        when(userService.getUserInfo(999L))
                .thenThrow(new BusinessException(Result.NOT_FOUND, "用户不存在"));

        mockMvc.perform(get("/api/user/info")
                        .requestAttr("userId", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }
}
