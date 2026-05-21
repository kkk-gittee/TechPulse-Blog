package com.blog.annotation;

import java.lang.annotation.*;

/**
 * 接口限流注解
 * 用于防止接口被恶意刷取
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    /**
     * 限流时间窗口（秒）
     */
    int time() default 60;

    /**
     * 时间窗口内最大请求次数
     */
    int count() default 60;

    /**
     * 限流提示信息
     */
    String message() default "请求过于频繁，请稍后再试";
}
