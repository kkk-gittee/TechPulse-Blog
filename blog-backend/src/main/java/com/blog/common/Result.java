package com.blog.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    /** 错误码常量 */
    public static final int SUCCESS = 200;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int CONFLICT = 409;
    public static final int RATE_LIMITED = 429;
    public static final int INTERNAL_ERROR = 500;

    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS, "success", data);
    }

    public static <T> Result<T> success() {
        return new Result<>(SUCCESS, "success", null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(INTERNAL_ERROR, message, null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}
