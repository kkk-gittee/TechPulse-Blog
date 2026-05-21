package com.blog.controller;

import com.blog.common.Result;
import com.blog.common.UserContext;
import com.blog.service.FollowService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "关注模块")
@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {
    private final FollowService followService;

    @ApiOperation("关注/取消关注")
    @PostMapping("/toggle/{userId}")
    public Result<Map<String, Boolean>> toggleFollow(@PathVariable Long userId, HttpServletRequest request) {
        Long currentUserId = UserContext.getCurrentUserId(request);
        Boolean followed = followService.toggleFollow(currentUserId, userId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("followed", followed);
        return Result.success(result);
    }

    @ApiOperation("检查是否已关注")
    @GetMapping("/check/{userId}")
    public Result<Map<String, Boolean>> checkFollow(@PathVariable Long userId, HttpServletRequest request) {
        Long currentUserId = UserContext.getCurrentUserId(request);
        Boolean followed = followService.isFollowed(currentUserId, userId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("followed", followed);
        return Result.success(result);
    }

    @ApiOperation("获取用户关注统计")
    @GetMapping("/stats/{userId}")
    public Result<Map<String, Integer>> getUserFollowStats(@PathVariable Long userId) {
        Map<String, Integer> stats = followService.getUserFollowStats(userId);
        return Result.success(stats);
    }
}
