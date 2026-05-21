package com.blog.aspect;

import com.blog.annotation.RateLimit;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitAspectTest {

    @Mock
    private RedisUtils redisUtils;
    @Mock
    private ProceedingJoinPoint joinPoint;
    @Mock
    private MethodSignature signature;
    @InjectMocks
    private RateLimitAspect aspect;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void withinLimit_proceeds() throws Throwable {
        Method method = TestController.class.getMethod("limited");
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(redisUtils.incrementWithExpire(anyString(), eq(60L))).thenReturn(1L);
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.around(joinPoint);

        assertEquals("ok", result);
    }

    @Test
    void exceedsLimit_throwsBusinessException() throws Throwable {
        Method method = TestController.class.getMethod("limited");
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(redisUtils.incrementWithExpire(anyString(), eq(60L))).thenReturn(21L);

        assertThrows(BusinessException.class, () -> aspect.around(joinPoint));
    }

    public static class TestController {
        @RateLimit(time = 60, count = 20, message = "请求过于频繁")
        public String limited() {
            return "ok";
        }
    }
}
