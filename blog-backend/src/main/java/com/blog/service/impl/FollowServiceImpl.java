package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.entity.Follow;
import com.blog.mapper.FollowMapper;
import com.blog.service.FollowService;
import com.blog.service.NotificationService;
import com.blog.common.BusinessException;
import com.blog.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {
    private final FollowMapper followMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean toggleFollow(Long userId, Long followUserId) {
        if (Objects.equals(userId, followUserId)) {
            throw new BusinessException(Result.BAD_REQUEST, "不能关注自己");
        }

        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId).eq(Follow::getFollowUserId, followUserId);
        Follow follow = followMapper.selectOne(wrapper);

        if (follow != null) {
            followMapper.deleteById(follow.getId());
            return false;
        } else {
            Follow newFollow = new Follow();
            newFollow.setUserId(userId);
            newFollow.setFollowUserId(followUserId);
            followMapper.insert(newFollow);

            notificationService.sendFollowNotification(followUserId, userId);

            return true;
        }
    }

    @Override
    public Boolean isFollowed(Long userId, Long followUserId) {
        if (userId == null || followUserId == null) return false;
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId).eq(Follow::getFollowUserId, followUserId);
        return followMapper.selectCount(wrapper) > 0;
    }

    @Override
    public Map<String, Integer> getUserFollowStats(Long userId) {
        Map<String, Integer> stats = new HashMap<>();

        LambdaQueryWrapper<Follow> followingWrapper = new LambdaQueryWrapper<>();
        followingWrapper.eq(Follow::getUserId, userId);
        stats.put("followingCount", followMapper.selectCount(followingWrapper).intValue());

        LambdaQueryWrapper<Follow> followerWrapper = new LambdaQueryWrapper<>();
        followerWrapper.eq(Follow::getFollowUserId, userId);
        stats.put("followerCount", followMapper.selectCount(followerWrapper).intValue());

        return stats;
    }
}
