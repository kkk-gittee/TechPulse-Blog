package com.blog.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class CommentDTO {
    @NotNull(message = "文章ID不能为空")
    private Long articleId;

    private Long parentId;

    private Long replyUserId;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 2000, message = "评论最长2000字")
    private String content;
}
