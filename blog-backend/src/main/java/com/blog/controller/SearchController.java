package com.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.Result;
import com.blog.common.UserContext;
import com.blog.dto.ArticleVO;
import com.blog.dto.SearchDTO;
import com.blog.service.SearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Api(tags = "搜索模块")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    @ApiOperation("搜索文章")
    @PostMapping("/articles")
    public Result<Page<ArticleVO>> searchArticles(
            @RequestBody SearchDTO searchDTO,
            HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserIdOrNull(request);
        Page<ArticleVO> result = searchService.searchArticles(searchDTO, userId);
        return Result.success(result);
    }
}
