package com.blog.common;

import javax.servlet.http.HttpServletRequest;

public class UserContext {

    /**
     * 获取当前登录用户 ID，未登录时返回 null。
     */
    public static Long getCurrentUserIdOrNull(HttpServletRequest request) {
        try {
            return (Long) request.getAttribute("userId");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前登录用户 ID，未登录时抛出异常。
     */
    public static Long getCurrentUserId(HttpServletRequest request) {
        Long userId = getCurrentUserIdOrNull(request);
        if (userId == null) {
            throw new BusinessException(Result.UNAUTHORIZED, "请先登录");
        }
        return userId;
    }
}
