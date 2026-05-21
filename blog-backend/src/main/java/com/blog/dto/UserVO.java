package com.blog.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private String bio;
    private Integer articleCount;
    private Integer followerCount;
    private Integer followingCount;
    private Boolean isFollowed;
    private LocalDateTime createTime;
}
