package com.blog.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AiSummaryRequest {
    @NotNull(message = "文章ID不能为空")
    private Long articleId;
}
