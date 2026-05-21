package com.blog.aspect;

import com.blog.annotation.Idempotent;
import com.blog.common.BusinessException;
import com.blog.common.Result;
import com.blog.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class IdempotentAspect {
    private final RedisUtils redisUtils;

    @Around("@annotation(com.blog.annotation.Idempotent)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Idempotent idempotent = method.getAnnotation(Idempotent.class);

        if (idempotent == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization");
        String methodPath = method.getDeclaringClass().getName() + "." + method.getName();
        String key = "idempotent:" + methodPath + ":" + (token != null ? sha256(token) : "anonymous");

        // 原子操作：SETNX，不存在则设置并返回 true，已存在则返回 false
        Boolean inserted = redisUtils.setIfAbsent(key, "1", idempotent.expire(), TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(inserted)) {
            log.warn("重复提交: method={}", methodPath);
            throw new BusinessException(Result.CONFLICT, idempotent.message());
        }

        return joinPoint.proceed();
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(input.hashCode());
        }
    }
}
