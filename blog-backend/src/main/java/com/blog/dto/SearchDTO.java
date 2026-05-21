package com.blog.dto;

import lombok.Data;

@Data
public class SearchDTO {
    private String keyword;

    private Long categoryId;

    private String tagName;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
