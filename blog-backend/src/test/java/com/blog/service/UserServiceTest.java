package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.BusinessException;
import com.blog.dto.UserDTO;
import com.blog.dto.UserVO;
import com.blog.entity.User;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.impl.UserServiceImpl;
import com.blog.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private ArticleMapper articleMapper;
    @Mock
    private FollowService followService;
    @InjectMocks
    private UserServiceImpl userService;

    // ========== register ==========

    @Test
    void register_success() {
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        UserDTO dto = new UserDTO();
        dto.setUsername("newuser");
        dto.setPassword("pass123");
        dto.setNickname("New User");

        assertDoesNotThrow(() -> userService.register(dto));
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void register_duplicateUsername_throws() {
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        UserDTO dto = new UserDTO();
        dto.setUsername("existing");
        dto.setPassword("pass123");

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.register(dto));
        assertEquals("用户名已存在", ex.getMessage());
        verify(userMapper, never()).insert(any());
    }

    // ========== login ==========

    @Test
    void login_success() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword(new BCryptPasswordEncoder().encode("secret"));
        user.setStatus(1);

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(jwtUtils.generateAccessToken(1L, "admin")).thenReturn("access-token");
        when(jwtUtils.generateRefreshToken(1L)).thenReturn("refresh-token");

        Map<String, String> tokens = userService.login("admin", "secret");

        assertNotNull(tokens);
        assertEquals("access-token", tokens.get("accessToken"));
        assertEquals("refresh-token", tokens.get("refreshToken"));
    }

    @Test
    void login_wrongPassword_throws() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword(new BCryptPasswordEncoder().encode("correct"));
        user.setStatus(1);

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.login("admin", "wrong"));
        assertEquals("用户名或密码错误", ex.getMessage());
    }

    @Test
    void login_userNotFound_throws() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> userService.login("nobody", "pass"));
    }

    @Test
    void login_disabledAccount_throws() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword(new BCryptPasswordEncoder().encode("pass"));
        user.setStatus(0);

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.login("admin", "pass"));
        assertEquals("账号已被禁用", ex.getMessage());
    }

    // ========== getUserInfo ==========

    @Test
    void getUserInfo_success() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setNickname("Admin");

        when(userMapper.selectById(1L)).thenReturn(user);
        when(articleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);
        when(followService.getUserFollowStats(1L)).thenReturn(
                Map.of("followingCount", 10, "followerCount", 20));

        UserVO vo = userService.getUserInfo(1L);

        assertNotNull(vo);
        assertEquals("admin", vo.getUsername());
        assertEquals(Integer.valueOf(5), vo.getArticleCount());
        assertEquals(Integer.valueOf(10), vo.getFollowingCount());
        assertEquals(Integer.valueOf(20), vo.getFollowerCount());
    }

    @Test
    void getUserInfo_notFound_throws() {
        when(userMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> userService.getUserInfo(999L));
    }

    // ========== updateUser ==========

    @Test
    void updateUser_success() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setNickname("Old Name");

        when(userMapper.selectById(1L)).thenReturn(user);

        UserDTO dto = new UserDTO();
        dto.setNickname("New Name");
        dto.setEmail("new@example.com");

        userService.updateUser(1L, dto);

        verify(userMapper).updateById(argThat(u ->
                "New Name".equals(u.getNickname()) && "new@example.com".equals(u.getEmail())));
    }

    @Test
    void updateUser_notFound_throws() {
        when(userMapper.selectById(999L)).thenReturn(null);

        UserDTO dto = new UserDTO();
        dto.setNickname("X");
        assertThrows(BusinessException.class, () -> userService.updateUser(999L, dto));
    }

    @Test
    void updateUser_passwordEncoded() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("old-hash");

        when(userMapper.selectById(1L)).thenReturn(user);

        UserDTO dto = new UserDTO();
        dto.setPassword("newplain");

        userService.updateUser(1L, dto);

        verify(userMapper).updateById(argThat(u -> {
            String encoded = u.getPassword();
            return !encoded.equals("newplain") && new BCryptPasswordEncoder().matches("newplain", encoded);
        }));
    }
}
