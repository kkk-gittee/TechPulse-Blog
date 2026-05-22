package com.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.annotation.RateLimit;
import com.blog.common.Result;
import com.blog.common.UserContext;
import com.blog.dto.ArticleDTO;
import com.blog.dto.ArticleVO;
import com.blog.service.ArticleService;
import com.blog.service.BrowseHistoryService;
import com.blog.service.FavoriteService;
import com.blog.service.HotArticleService;
import com.blog.service.LikeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "文章模块")
@RestController
@RequestMapping("/api/article")
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;
    private final LikeService likeService;
    private final FavoriteService favoriteService;
    private final BrowseHistoryService browseHistoryService;
    private final HotArticleService hotArticleService;

    @ApiOperation("发布文章")
    @PostMapping("/create")
    @RateLimit(time = 60, count = 5, message = "发布文章过于频繁，请1分钟后再试")
    public Result<Void> createArticle(HttpServletRequest request, @Valid @RequestBody ArticleDTO articleDTO) {
        Long userId = UserContext.getCurrentUserId(request);
        articleService.createArticle(userId, articleDTO);
        return Result.success();
    }

    @ApiOperation("获取文章详情")
    @GetMapping("/detail/{id}")
    public Result<ArticleVO> getArticleDetail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserIdOrNull(request);
        ArticleVO articleVO = articleService.getArticleDetail(id, userId);
        if (userId != null) {
            articleVO.setIsLiked(likeService.isLiked(userId, id));
            articleVO.setIsFavorited(favoriteService.isFavorited(userId, id));
            // 记录浏览历史
            browseHistoryService.addHistory(userId, id);
        }
        articleVO.setFavoriteCount(favoriteService.countByArticle(id).intValue());
        // 记录热门文章
        hotArticleService.recordView(id);
        return Result.success(articleVO);
    }

    @ApiOperation("文章列表")
    @GetMapping("/list")
    public Result<Page<ArticleVO>> getArticleList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        pageSize = Math.min(pageSize, 100);
        Long userId = UserContext.getCurrentUserIdOrNull(request);
        Page<ArticleVO> page = articleService.getArticleList(pageNum, pageSize, userId);
        return Result.success(page);
    }

    @ApiOperation("更新文章")
    @PutMapping("/update/{id}")
    public Result<Void> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody ArticleDTO articleDTO,
            HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request);
        articleService.updateArticle(userId, id, articleDTO);
        return Result.success();
    }

    @ApiOperation("删除文章")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id, HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request);
        articleService.deleteArticle(userId, id);
        return Result.success();
    }

    @ApiOperation("点赞/取消点赞")
    @PostMapping("/like/{id}")
    public Result<Map<String, Boolean>> toggleLike(@PathVariable Long id, HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request);
        Boolean liked = likeService.toggleLike(userId, id);
        Map<String, Boolean> result = new HashMap<>();
        result.put("liked", liked);
        return Result.success(result);
    }

    @ApiOperation("获取用户文章")
    @GetMapping("/user/{userId}")
    public Result<Page<ArticleVO>> getUserArticles(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        pageSize = Math.min(pageSize, 100);
        Long currentUserId = UserContext.getCurrentUserIdOrNull(request);
        Page<ArticleVO> articles = articleService.getUserArticles(userId, pageNum, pageSize, currentUserId);
        return Result.success(articles);
    }
}
