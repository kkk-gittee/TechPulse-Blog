package com.blog.service;

import java.util.Map;

public interface FollowService {
    Boolean toggleFollow(Long userId, Long followUserId);

    Boolean isFollowed(Long userId, Long followUserId);

    Map<String, Integer> getUserFollowStats(Long userId);
}
