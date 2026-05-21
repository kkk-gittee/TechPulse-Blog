package com.blog.aspect;

import com.blog.annotation.Idempotent;
import com.blog.common.BusinessException;
import com.blog.utils.RedisUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotentAspectTest {

    @Mock
    private RedisUtils redisUtils;
    @Mock
    private ProceedingJoinPoint joinPoint;
    @Mock
    private MethodSignature signature;
    @InjectMocks
    private IdempotentAspect aspect;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer test-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void firstRequest_proceeds() throws Throwable {
        Method method = TestController.class.getMethod("doSomething");
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(redisUtils.setIfAbsent(anyString(), eq("1"), eq(5L), eq(TimeUnit.SECONDS))).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.around(joinPoint);

        assertEquals("ok", result);
    }

    @Test
    void duplicateRequest_throwsBusinessException() throws Throwable {
        Method method = TestController.class.getMethod("doSomething");
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(redisUtils.setIfAbsent(anyString(), eq("1"), eq(5L), eq(TimeUnit.SECONDS))).thenReturn(false);

        assertThrows(BusinessException.class, () -> aspect.around(joinPoint));
    }

    // Test controller with @Idempotent annotation
    public static class TestController {
        @Idempotent(expire = 5, message = "请勿重复提交")
        public String doSomething() {
            return "ok";
        }
    }
}
