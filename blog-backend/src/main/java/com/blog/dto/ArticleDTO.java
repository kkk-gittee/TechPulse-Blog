package com.blog.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class ArticleDTO {
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最长100字")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 100000, message = "内容最长10万字")
    private String content;

    @Size(max = 500, message = "摘要最长500字")
    private String summary;

    private String coverImage;

    private Long categoryId;

    private String tags;
}
