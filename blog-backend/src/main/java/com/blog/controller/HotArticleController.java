package com.blog.controller;

import com.blog.common.Result;
import com.blog.common.UserContext;
import com.blog.dto.ArticleVO;
import com.blog.service.HotArticleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags = "热门文章模块")
@RestController
@RequestMapping("/api/hot")
@RequiredArgsConstructor
public class HotArticleController {
    private final HotArticleService hotArticleService;

    @ApiOperation("获取热门文章")
    @GetMapping("/list")
    public Result<List<ArticleVO>> getHotArticles(
            @RequestParam(defaultValue = "10") Integer limit,
            HttpServletRequest request) {
        limit = Math.min(limit, 100);
        Long userId = UserContext.getCurrentUserIdOrNull(request);
        List<ArticleVO> articles = hotArticleService.getHotArticles(limit, userId);
        return Result.success(articles);
    }

    @ApiOperation("清除热门文章缓存（仅管理员）")
    @DeleteMapping("/clear")
    public Result<Void> clearHotArticles(HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request);
        // 简单校验：只有 admin 用户可以清除（userId=1 为 admin）
        if (!Long.valueOf(1L).equals(userId)) {
            return Result.error(Result.FORBIDDEN, "无权执行此操作");
        }
        hotArticleService.clearHotArticles();
        return Result.success();
    }
}
