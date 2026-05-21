package com.blog.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ArticleVO {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String title;
    private String summary;
    private String coverImage;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer status;
    private Long categoryId;
    private String categoryName;
    private String tags;
    private LocalDateTime createTime;
    private Boolean isLiked;
    private Boolean isFavorited;
}
