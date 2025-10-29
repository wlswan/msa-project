package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    DUPLICATE_USERNAME("이미 존재하는 아이디입니다.", HttpStatus.CONFLICT),
    INVALID_PASSWORD("비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND("존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),
    INVALID_TOKEN("유효하지 않은 JWT 토큰입니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN("토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    MISSING_TOKEN("토큰이 존재하지 않습니다.",HttpStatus.UNAUTHORIZED);

    private final String message;
    private final HttpStatus status;

    ErrorCode(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
