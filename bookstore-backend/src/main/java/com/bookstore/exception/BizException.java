package com.bookstore.exception;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException {
    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public static BizException badRequest(String msg) { return new BizException(400, msg); }
    public static BizException unauthorized(String msg) { return new BizException(401, msg); }
    public static BizException forbidden(String msg) { return new BizException(403, msg); }
    public static BizException notFound(String msg) { return new BizException(404, msg); }
}
