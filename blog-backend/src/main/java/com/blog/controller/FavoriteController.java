package com.blog.controller;

import com.blog.common.Result;
import com.blog.common.UserContext;
import com.blog.dto.ArticleVO;
import com.blog.service.FavoriteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "收藏模块")
@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;

    @ApiOperation("收藏/取消收藏")
    @PostMapping("/toggle/{articleId}")
    public Result<Map<String, Boolean>> toggleFavorite(@PathVariable Long articleId, HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request);
        Boolean favorited = favoriteService.toggleFavorite(userId, articleId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("favorited", favorited);
        return Result.success(result);
    }

    @ApiOperation("检查是否已收藏")
    @GetMapping("/check/{articleId}")
    public Result<Map<String, Boolean>> checkFavorite(@PathVariable Long articleId, HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request);
        Boolean favorited = favoriteService.isFavorited(userId, articleId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("favorited", favorited);
        return Result.success(result);
    }

    @ApiOperation("获取用户收藏的文章ID列表")
    @GetMapping("/list")
    public Result<List<Long>> getUserFavorites(HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request);
        List<Long> articleIds = favoriteService.getUserFavoriteArticleIds(userId);
        return Result.success(articleIds);
    }

    @ApiOperation("获取用户收藏的文章详情列表")
    @GetMapping("/articles")
    public Result<List<ArticleVO>> getUserFavoriteArticles(HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request);
        List<ArticleVO> articles = favoriteService.getUserFavoriteArticles(userId);
        return Result.success(articles);
    }
}
