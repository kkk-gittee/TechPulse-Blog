package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.Category;
import com.blog.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "分类模块")
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @ApiOperation("获取分类列表")
    @GetMapping("/list")
    public Result<List<Category>> listCategories() {
        List<Category> categories = categoryService.listCategories();
        return Result.success(categories);
    }

    @ApiOperation("获取分类详情")
    @GetMapping("/{id}")
    public Result<Category> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return Result.success(category);
    }
}
