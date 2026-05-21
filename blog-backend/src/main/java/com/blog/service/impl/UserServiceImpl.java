package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.BusinessException;
import com.blog.common.Result;
import com.blog.dto.UserDTO;
import com.blog.dto.UserVO;
import com.blog.entity.Article;
import com.blog.entity.User;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.FollowService;
import com.blog.service.UserService;
import com.blog.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final ArticleMapper articleMapper;
    private final FollowService followService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void register(UserDTO userDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, userDTO.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(Result.BAD_REQUEST, "用户名已存在");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setNickname(userDTO.getNickname() != null ? userDTO.getNickname() : userDTO.getUsername());
        user.setAvatar(userDTO.getAvatar());
        user.setEmail(userDTO.getEmail());
        user.setBio(userDTO.getBio());
        user.setStatus(1);
        user.setDeleted(0);
        userMapper.insert(user);
    }

    @Override
    public Map<String, String> login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);

        // 统一返回"用户名或密码错误"，防止用户名枚举攻击
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(Result.UNAUTHORIZED, "用户名或密码错误");
        }

        if (user.getStatus() != 1) {
            throw new BusinessException(Result.FORBIDDEN, "账号已被禁用");
        }

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", jwtUtils.generateAccessToken(user.getId(), user.getUsername()));
        tokens.put("refreshToken", jwtUtils.generateRefreshToken(user.getId()));
        return tokens;
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(Result.NOT_FOUND, "用户不存在");
        }
        return convertToVO(user);
    }

    @Override
    public UserVO getPublicUserInfo(Long userId, Long currentUserId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(Result.NOT_FOUND, "用户不存在");
        }
        UserVO vo = convertToVO(user);
        vo.setEmail(null); // 公开端点不暴露邮箱
        if (currentUserId != null) {
            vo.setIsFollowed(followService.isFollowed(currentUserId, userId));
        }
        return vo;
    }

    @Override
    public void updateUser(Long userId, UserDTO userDTO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(Result.NOT_FOUND, "用户不存在");
        }

        if (userDTO.getNickname() != null) user.setNickname(userDTO.getNickname());
        if (userDTO.getAvatar() != null) user.setAvatar(userDTO.getAvatar());
        if (userDTO.getEmail() != null) user.setEmail(userDTO.getEmail());
        if (userDTO.getBio() != null) user.setBio(userDTO.getBio());
        if (userDTO.getPassword() != null) user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        userMapper.updateById(user);
    }

    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setBio(user.getBio());
        vo.setCreateTime(user.getCreateTime());

        LambdaQueryWrapper<Article> articleWrapper = new LambdaQueryWrapper<>();
        articleWrapper.eq(Article::getUserId, user.getId()).eq(Article::getStatus, 1);
        vo.setArticleCount(articleMapper.selectCount(articleWrapper).intValue());

        Map<String, Integer> stats = followService.getUserFollowStats(user.getId());
        vo.setFollowingCount(stats.get("followingCount"));
        vo.setFollowerCount(stats.get("followerCount"));

        return vo;
    }
}
