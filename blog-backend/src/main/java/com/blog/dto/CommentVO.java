package com.blog.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {
    private Long id;
    private Long articleId;
    private Long userId;
    private Long parentId;
    private Long replyUserId;
    private String content;
    private Integer likeCount;
    private LocalDateTime createTime;

    private String username;
    private String nickname;
    private String avatar;

    private String replyUsername;
    private String replyNickname;

    private Boolean isLiked;
    private List<CommentVO> replies;
}
