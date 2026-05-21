package com.blog.controller;

import com.blog.annotation.Idempotent;
import com.blog.annotation.RateLimit;
import com.blog.common.Result;
import com.blog.common.UserContext;
import com.blog.dto.LoginDTO;
import com.blog.dto.UserDTO;
import com.blog.dto.UserVO;
import com.blog.service.UserService;
import com.blog.utils.JwtUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "用户模块")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtUtils jwtUtils;

    @ApiOperation("用户注册")
    @PostMapping("/register")
    @RateLimit(time = 60, count = 5, message = "注册请求过于频繁，请1分钟后再试")
    @Idempotent(expire = 3, message = "请勿重复注册")
    public Result<Void> register(@Valid @RequestBody UserDTO userDTO) {
        userService.register(userDTO);
        return Result.success();
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    @RateLimit(time = 60, count = 10, message = "登录请求过于频繁，请1分钟后再试")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
        Map<String, String> tokens = userService.login(loginDTO.getUsername(), loginDTO.getPassword());
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", tokens.get("accessToken"));
        result.put("refreshToken", tokens.get("refreshToken"));
        return Result.success(result);
    }

    @ApiOperation("刷新 AccessToken")
    @PostMapping("/refresh")
    public Result<Map<String, String>> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            return Result.error(Result.BAD_REQUEST, "RefreshToken 不能为空");
        }
        try {
            Long userId = jwtUtils.validateRefreshToken(refreshToken);
            String username = jwtUtils.getUsername(refreshToken);
            String newAccessToken = jwtUtils.generateAccessToken(userId, username);
            Map<String, String> result = new HashMap<>();
            result.put("accessToken", newAccessToken);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(Result.UNAUTHORIZED, "RefreshToken 无效或已过期");
        }
    }

    @ApiOperation("获取当前用户信息")
    @GetMapping("/info")
    public Result<UserVO> getCurrentUserInfo(HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request);
        UserVO userVO = userService.getUserInfo(userId);
        return Result.success(userVO);
    }

    @ApiOperation("获取用户公开信息")
    @GetMapping("/public/{userId}")
    public Result<UserVO> getPublicUserInfo(@PathVariable Long userId, HttpServletRequest request) {
        Long currentUserId = UserContext.getCurrentUserIdOrNull(request);
        UserVO userVO = userService.getPublicUserInfo(userId, currentUserId);
        return Result.success(userVO);
    }

    @ApiOperation("更新用户信息")
    @PutMapping("/update")
    public Result<Void> updateUser(HttpServletRequest request, @Valid @RequestBody UserDTO userDTO) {
        Long userId = UserContext.getCurrentUserId(request);
        userService.updateUser(userId, userDTO);
        return Result.success();
    }
}
