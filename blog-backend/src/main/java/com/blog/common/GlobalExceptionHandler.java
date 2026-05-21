package com.blog.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：根据错误码返回对应的 HTTP 状态码 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        log.warn("业务异常 [{}]: {}", e.getCode(), e.getMessage());
        HttpStatus status = mapHttpStatus(e.getCode());
        return ResponseEntity.status(status).body(Result.error(e.getCode(), e.getMessage()));
    }

    /** 未分类的 RuntimeException：统一返回 500 */
    @ExceptionHandler(RuntimeException.class)
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.error("未处理异常: {}", e.getMessage(), e);
        return Result.error(Result.INTERNAL_ERROR, "系统内部错误");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        return Result.error(Result.BAD_REQUEST, message);
    }

    @ExceptionHandler(BindException.class)
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return Result.error(Result.BAD_REQUEST, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        return Result.error(Result.BAD_REQUEST, message);
    }

    @ExceptionHandler(Exception.class)
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.error(Result.INTERNAL_ERROR, "系统内部错误");
    }

    private HttpStatus mapHttpStatus(Integer code) {
        if (code == null) return HttpStatus.INTERNAL_SERVER_ERROR;
        switch (code) {
            case Result.UNAUTHORIZED: return HttpStatus.UNAUTHORIZED;     // 401 — 登录失败
            case Result.FORBIDDEN:   return HttpStatus.FORBIDDEN;        // 403 — 账号禁用
            case Result.NOT_FOUND:   return HttpStatus.NOT_FOUND;        // 404 — 用户不存在
            case Result.CONFLICT:    return HttpStatus.CONFLICT;         // 409 — 重复提交
            case Result.RATE_LIMITED:return HttpStatus.TOO_MANY_REQUESTS;// 429 — 限流
            case Result.BAD_REQUEST: return HttpStatus.BAD_REQUEST;       // 400 — 参数错误
            default:                 return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
