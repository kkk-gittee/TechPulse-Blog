package com.blog.annotation;

import java.lang.annotation.*;

/**
 * 接口幂等性注解
 * 用于防止重复提交
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {
    /**
     * 幂等键的过期时间（秒）
     */
    int expire() default 5;

    /**
     * 重复提交提示信息
     */
    String message() default "请勿重复提交";
}
