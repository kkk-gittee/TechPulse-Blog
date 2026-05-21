package com.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.Result;
import com.blog.common.UserContext;
import com.blog.dto.ArticleVO;
import com.blog.service.BrowseHistoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Api(tags = "浏览历史模块")
@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {
    private final BrowseHistoryService browseHistoryService;

    @ApiOperation("获取浏览历史")
    @GetMapping("/list")
    public Result<Page<ArticleVO>> getHistory(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        pageSize = Math.min(pageSize, 100);
        Long userId = UserContext.getCurrentUserId(request);
        Page<ArticleVO> page = browseHistoryService.getUserHistory(userId, pageNum, pageSize);
        return Result.success(page);
    }

    @ApiOperation("清空浏览历史")
    @DeleteMapping("/clear")
    public Result<Void> clearHistory(HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request);
        browseHistoryService.clearHistory(userId);
        return Result.success();
    }
}
