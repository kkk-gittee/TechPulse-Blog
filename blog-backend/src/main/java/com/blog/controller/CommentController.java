package com.blog.controller;

import com.blog.common.Result;
import com.blog.common.UserContext;
import com.blog.dto.CommentDTO;
import com.blog.dto.CommentVO;
import com.blog.service.CommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Api(tags = "评论模块")
@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @ApiOperation("添加评论")
    @PostMapping("/add")
    public Result<Void> addComment(HttpServletRequest request, @Valid @RequestBody CommentDTO commentDTO) {
        Long userId = UserContext.getCurrentUserId(request);
        commentService.addComment(userId, commentDTO);
        return Result.success();
    }

    @ApiOperation("获取文章评论")
    @GetMapping("/list/{articleId}")
    public Result<List<CommentVO>> getComments(@PathVariable Long articleId, HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserIdOrNull(request);
        List<CommentVO> comments = commentService.getArticleComments(articleId, userId);
        return Result.success(comments);
    }

    @ApiOperation("删除评论")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteComment(@PathVariable Long id, HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request);
        commentService.deleteComment(userId, id);
        return Result.success();
    }

    @ApiOperation("点赞/取消点赞评论")
    @PostMapping("/like/{id}")
    public Result<Map<String, Boolean>> toggleLikeComment(@PathVariable Long id, HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request);
        Boolean liked = commentService.toggleLikeComment(userId, id);
        return Result.success(Map.of("liked", liked));
    }
}
