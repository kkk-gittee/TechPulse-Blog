package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.Tag;
import com.blog.service.TagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "标签模块")
@RestController
@RequestMapping("/api/tag")
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;

    @ApiOperation("获取热门标签")
    @GetMapping("/hot")
    public Result<List<Tag>> getHotTags(@RequestParam(defaultValue = "20") Integer limit) {
        limit = Math.min(limit, 100);
        List<Tag> tags = tagService.listHotTags(limit);
        return Result.success(tags);
    }

    @ApiOperation("获取所有标签")
    @GetMapping("/list")
    public Result<List<Tag>> getAllTags() {
        List<Tag> tags = tagService.listAllTags();
        return Result.success(tags);
    }
}
