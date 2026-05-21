package com.blog.service;

import com.blog.dto.UserDTO;
import com.blog.dto.UserVO;

import java.util.Map;

public interface UserService {
    void register(UserDTO userDTO);

    Map<String, String> login(String username, String password);

    UserVO getUserInfo(Long userId);

    UserVO getPublicUserInfo(Long userId, Long currentUserId);

    void updateUser(Long userId, UserDTO userDTO);
}
